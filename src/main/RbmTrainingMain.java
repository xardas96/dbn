package main;

import io.ObjectIOManager;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import testsets.mfcc.MfccParams;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.deep.DeepBoltzmannMachineTrainer;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.vectors.InputStateVector;

public class RbmTrainingMain {
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_new";

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		File file = new File(SAVE_PATH + "\\stats.info");
		if (file.exists()) {
			final String stats = ObjectIOManager.load(file);
			File path = new File(SAVE_PATH);
			File[] list = path.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.getName().contains(stats);
				}
			});
			List<InputStateVector> training = ObjectIOManager.load(new File("training.data"));
			DeepBoltzmannMachine dbm = ObjectIOManager.load(list[0]);
			dbm.createThreadManager();
			File learningF = new File(SAVE_PATH + "\\learning.factor");
			AdaptiveLearningFactor lf;
			if (learningF.exists()) {
				lf = ObjectIOManager.load(new File("learning.factor"));
			} else {
				lf = new AdaptiveLearningFactor(0.001, 0.8, 1.5);
			}
			DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, lf, 100, Double.MIN_VALUE);
			String[] split = stats.split("_");
			int lastLayer = Integer.valueOf(split[0]);
			int lastEpoch = Integer.valueOf(split[2]);
			trainer.setStartLayer(lastLayer);
			trainer.setStart(lastEpoch + 1);
			trainer.train(training);
		} else {
			List<InputStateVector> training = ObjectIOManager.load(new File("training.data"));
			Integer[] layers = new Integer[] { MfccParams.VECTOR_SIZE, 1024, 1024 };
			DeepBoltzmannMachine dbm = BoltzmannMachineFactory.getDeepBotlzmannMachine(true, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), layers);
			dbm.createThreadManager();
			DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, new AdaptiveLearningFactor(0.001, 0.8, 1.5), 100, Double.MIN_VALUE);
			trainer.train(training);
		}
	}
}
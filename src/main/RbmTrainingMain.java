package main;

import io.ObjectIOManager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import testsets.mfcc.MfccParams;
import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.deep.DeepBoltzmannMachineTrainer;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.training.learningfactor.ConstantLearningFactor;
import boltzmann.units.UnitType;
import boltzmann.vectors.InputStateVector;

public class RbmTrainingMain {
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_new";
	private static final double LEARINING_FACTOR_FOR_GAUSSIAN = 0.00001;
	private static final double LEARINING_FACTOR_FOR_BINARY = 0.001;
	private static final int HIDDEN_UNITS_COUNT = 1024;
	private static final int TRAINING_EPOCHS = 200;
	private static final double INITIAL_MOMENTUM = 0.5;
	private static final double FINAL_MOMENTUM = 0.9;
	private static final int CD_K = 1;
	private static final double DROP_OUT_PROBABILITY = 0;

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		File file = new File(SAVE_PATH + "\\stats.info");
		if (file.exists()) {
			final String stats = ObjectIOManager.load(file);
			String[] split = stats.split("_");
			int lastLayer = Integer.valueOf(split[0]);
			int lastEpoch = Integer.valueOf(split[2]);
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
				if (lastLayer == 0) {
					lf = new ConstantLearningFactor(LEARINING_FACTOR_FOR_GAUSSIAN);
				} else {
					lf = new ConstantLearningFactor(LEARINING_FACTOR_FOR_BINARY);
				}
			}
//			addExtraLayer(dbm);
			DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, lf, TRAINING_EPOCHS, Double.MIN_VALUE, INITIAL_MOMENTUM, FINAL_MOMENTUM, CD_K, DROP_OUT_PROBABILITY);
			trainer.setStartLayer(lastLayer);
			trainer.setStart(lastEpoch + 1);
			trainer.train(training);
		} else {
			List<InputStateVector> training = ObjectIOManager.load(new File("training.data"));
			Integer[] layers = new Integer[] { MfccParams.VECTOR_SIZE, HIDDEN_UNITS_COUNT, HIDDEN_UNITS_COUNT };
			DeepBoltzmannMachine dbm = BoltzmannMachineFactory.getDeepBotlzmannMachine(true, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), layers);
			dbm.createThreadManager();
			DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, new ConstantLearningFactor(LEARINING_FACTOR_FOR_GAUSSIAN), TRAINING_EPOCHS, Double.MIN_VALUE, INITIAL_MOMENTUM, FINAL_MOMENTUM, CD_K, DROP_OUT_PROBABILITY);
			trainer.train(training);
		}
	}

	private static void addExtraLayer(DeepBoltzmannMachine dbm) throws IOException {
		if (!new File(SAVE_PATH + "\\" + "added.layer").exists()) {
			Layer extraLayer = new Layer(HIDDEN_UNITS_COUNT, UnitType.HIDDEN);
			Layer dbnLastLayer = dbm.getLayers().get(dbm.getLayers().size() - 1);
			LayerConnector connector = new LayerConnector(dbnLastLayer, extraLayer, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer());
			dbm.getLayers().add(extraLayer);
			dbm.getConnections().add(connector);
			dbm.getBiases().add(new Layer(HIDDEN_UNITS_COUNT, UnitType.BIAS));
			ObjectIOManager.save("x", new File("added.layer"));
		}
	}
}
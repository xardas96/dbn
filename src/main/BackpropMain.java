package main;

import io.ObjectIOManager;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import testsets.mfcc.MfccFileParser;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.training.TrainingBatchCompletedListener;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.vectors.InputStateVector;
import dbn.BackpropagationDeepBeliefNetworkTrainer;
import dbn.DeepBeliefNetwork;

public class BackpropMain {
	private static final String PATH = "E:\\Dropbox\\mgr-asr";
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_new";
	private static final int NUMBER_OF_EPOCHS = 25;
	private static final double MOMENTUM = 0.9;
	private static final AdaptiveLearningFactor LEARNING_FACTOR = new AdaptiveLearningFactor(0.02, 1, 1);

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		List<InputStateVector> training = ObjectIOManager.load(new File("training.data"));
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

			if (stats.contains("backprop")) {
				final DeepBeliefNetwork dbn = ObjectIOManager.load(list[0]);
				String[] split = stats.split("_");
				int start = Integer.valueOf(split[1]);
				BackpropagationDeepBeliefNetworkTrainer backPropTrainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, LEARNING_FACTOR, NUMBER_OF_EPOCHS, MOMENTUM);
				backPropTrainer.addTrainingBatchCompletedListener(new TrainingBatchCompletedListener() {

					@Override
					public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
						System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
					}
				});
				backPropTrainer.setStart(start + 1);
				backPropTrainer.train(training);
			} else {
				MfccFileParser p = new MfccFileParser(PATH);
				List<String> phones = p.getPhones(new File(PATH + "\\phones"), true);
				DeepBoltzmannMachine dbm = ObjectIOManager.load(list[0]);
				final DeepBeliefNetwork dbn = BoltzmannMachineFactory.getDeepBeliefNetwork(dbm, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), phones.size());
				BackpropagationDeepBeliefNetworkTrainer backPropTrainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, LEARNING_FACTOR, NUMBER_OF_EPOCHS, MOMENTUM);
				backPropTrainer.addTrainingBatchCompletedListener(new TrainingBatchCompletedListener() {

					@Override
					public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
						System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
					}
				});
				backPropTrainer.train(training);
			}
		}
	}
}
package main;

import io.ObjectIOManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import mnist.MNISTDigitElement;
import mnist.MNISTReader;
import mnist.MnistPanel;
import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.deep.DeepBoltzmannMachineTrainer;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.training.TrainingBatchCompletedListener;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;
import dbn.BackpropagationDeepBeliefNetworkTrainer;
import dbn.DeepBeliefNetwork;

public class Main {

	public static void main(String[] args) {
		if (args.length > 0) {
			switch (args[0]) {
			case "rbm":
				testRestrictedBoltzmannMachine();
				return;
			case "dbm":
				testDeepBoltzmannMachine();
				return;
			case "dbn":
				testDeepBeliefNetwork();
				return;
			}
		} else {
			// testRestrictedBoltzmannMachine();
			// testDeepBoltzmannMachine();
			// testDeepBeliefNetwork();
			testClassification();
		}
	}

	private static void testRestrictedBoltzmannMachine() {
		final MnistPanel p = new MnistPanel();
		JFrame f = new JFrame();
		f.setContentPane(p);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(100);
		}
		List<InputStateVector> training = reader.getTrainingSetItems();
		final int numVisible = reader.getCols() * reader.getRows();
		final int numHidden = 10 * 10;

		final List<MNISTDigitElement> tests = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			MNISTDigitElement test = reader.getTestItem(i);
			test.binarize();
			tests.add(test);
		}

		final RestrictedBoltzmannMachine rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(LayerConnectorWeightInitializerFactory.getGaussianWeightInitializer(), numVisible, numHidden);
		RestrictedBoltzmannMachineTrainer t = new RestrictedBoltzmannMachineTrainer(rbm, new AdaptiveLearningFactor(), 500, Double.MIN_VALUE);
		t.addTrainingStepCompletedListener(new TrainingStepCompletedListener() {

			@Override
			public void onTrainingStepComplete(int step, int trainingBatchSize) {
				if (step % 100 == 0) {
					System.out.println("step " + step + "/" + trainingBatchSize);
				}
			}

			@Override
			public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
				List<int[]> outputs = new ArrayList<>();
				for (int i = 0; i < tests.size(); i++) {
					MNISTDigitElement test = tests.get(i);
					rbm.resetStates();
					rbm.initializeVisibleLayerStates(test);
					rbm.updateHiddenUnits();
					rbm.resetVisibleStates();
					rbm.reconstructVisibleUnits();
					rbm.reconstructHiddenUnits();
					double[] vis = rbm.getVisibleLayerStates();
					int[] output = new int[vis.length];
					for (int j = 0; j < vis.length; j++) {
						output[j] = (int) Math.round(vis[j] * 255.0f);
					}
					outputs.add(output);
				}
				p.setOutputs(outputs);
				p.repaint();
				System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
			}
		});
		t.train(training);
		try {
			ObjectIOManager.save(rbm, new File("mnist.rbm"));
		} catch (Exception e) {
		}
	}

	private static void testDeepBoltzmannMachine() {
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(200);
		}
		List<InputStateVector> training = reader.getTrainingSetItems();
		DeepBoltzmannMachine dbm = BoltzmannMachineFactory.getDeepBotlzmannMachine(LayerConnectorWeightInitializerFactory.getGaussianWeightInitializer(), 784, 1000, 1000, 1000);
		DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, new AdaptiveLearningFactor(0.01, 1, 1), 100, Double.MIN_VALUE);
		trainer.train(training);
		try {
			ObjectIOManager.save(dbm, new File("deep_boltzmann.dbm"));
		} catch (Exception e) {
		}
	}

	private static void testDeepBeliefNetwork() {
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(200);
		}
		List<InputStateVector> training = reader.getTrainingSetItems();
		try {
			DeepBoltzmannMachine dbm = ObjectIOManager.load(new File("E:\\Dropbox\\rbm test\\deep_boltzmann.dbm"));
			dbm.createThreadManager();
			DeepBeliefNetwork dbn = BoltzmannMachineFactory.getDeepBeliefNetwork(dbm, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), 10);
			BackpropagationDeepBeliefNetworkTrainer trainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, new AdaptiveLearningFactor(0.2, 1, 1), 25);
			trainer.addTrainingBatchCompletedListener(new TrainingBatchCompletedListener() {

				@Override
				public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
					System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
				}
			});
			trainer.train(training);
			ObjectIOManager.save(dbn, new File("dbn_backproped.dbn"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void testClassification() {
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(100);
		}
		List<InputStateVector> testing = reader.getTestingSetItems();
		try {
			int count = 0;
			DeepBeliefNetwork dbn = ObjectIOManager.load(new File("E:\\Dropbox\\rbm test\\dbn_backproped.dbn"));
			for (InputStateVector vector : testing) {
				dbn.resetStates();
				dbn.getFirstLayer().initStates(vector);
				for (int j = 1; j < dbn.getLayers().size(); j++) {
					Layer outputLayer = dbn.getLayers().get(j);
					dbn.updateUnits(outputLayer);
				}
				Layer last = dbn.getLastLayer();
				int maxIndex = -1;
				double maxProb = Double.MIN_VALUE;
				for (int i = 0; i < last.size(); i++) {
					Unit u = last.getUnit(i);
					double prob = u.getActivationProbability();
					if (prob > maxProb) {
						maxProb = prob;
						maxIndex = i;
					}
				}
				int lab = Integer.valueOf(vector.getLabel());
				if (lab == maxIndex) {
					count++;
				}
				System.out.println(testing.indexOf(vector) + ": " + count + "/" + testing.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package main;

import io.ObjectIOManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import boltzmann.training.TrainingStepCompletedListener;
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
			testDeepBeliefNetwork();
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
			// InputStateVector testVector = training.get(2);
			// InputStateVector tempVector = new
			// InputStateVector(testVector.getInputStates());
			// for (int j = 0; j < dbm.getLayers().size() - 1; j++) {
			// dbm.resetStates();
			// dbm.initializeVisibleLayerStates(tempVector);
			// dbm.updateHiddenUnits();
			// tempVector.setInputStates(dbm.getHiddenLayerStates());
			// if (j != dbm.getLayers().size() - 2) {
			// dbm.ascendLayers();
			// }
			// }
			// for (int j = dbm.getLayers().size() - 1; j >= 1; j--) {
			// dbm.resetStates();
			// dbm.initializeHiddenLayerStates(tempVector);
			// dbm.reconstructVisibleUnits();
			// tempVector.setInputStates(dbm.getVisibleLayerStates());
			// if (j != 1) {
			// dbm.descendLayers();
			// }
			// }
			// int[] output = new int[tempVector.size()];
			// for (int i = 0; i < tempVector.size(); i++) {
			// output[i] = (int) Math.round(tempVector.get(i) * 255.0f);
			// }
			//
			// BufferedImage out = new BufferedImage(28, 28,
			// BufferedImage.TYPE_INT_RGB);
			// WritableRaster r = out.getRaster();
			// r.setDataElements(0, 0, 28, 28, output);
			// ImageIO.write(out, "png", new File("test.png"));

			DeepBeliefNetwork dbn = BoltzmannMachineFactory.getDeepBeliefNetwork(dbm, LayerConnectorWeightInitializerFactory.getGaussianWeightInitializer(), 10);
			BackpropagationDeepBeliefNetworkTrainer trainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, new AdaptiveLearningFactor(), 25);
			trainer.train(training);
			ObjectIOManager.save(dbm, new File("dbn_backproped.dbn"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void testClassification() {
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(100);
		}
		List<InputStateVector> training = reader.getTrainingSetItems();
		List<InputStateVector> testing = reader.getTestingSetItems();

		try {
			DeepBeliefNetwork dbn = ObjectIOManager.load(new File("E:\\Dropbox\\rbm test\\dbn_backproped.dbn"));
			dbn.resetStates();

			InputStateVector vector = testing.get(0);

			double[] output = vector.getOutputStates();
			dbn.getFirstLayer().initStates(vector);
			for (int j = 1; j < dbn.getLayers().size(); j++) {
				Layer outputLayer = dbn.getLayers().get(j);
				dbn.updateUnits(outputLayer);
			}
			Layer last = dbn.getLastLayer();
			System.out.println(Arrays.toString(output) + Arrays.toString(dbn.getLastLayer().getStates()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
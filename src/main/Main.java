package main;

import io.ObjectIOManager;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

import testsets.mfcc.MfccFileParser;
import testsets.mfcc.MfccParams;
import testsets.mfcc.MfccSample;
import testsets.mfcc.PhoneTiming;
import testsets.mnist.MNISTDigitElement;
import testsets.mnist.MNISTReader;
import testsets.mnist.MnistPanel;
import utils.ConfusionMatrix;
import utils.GaussianDistributionDataNormalizer;
import utils.TrainingSetGenerator;
import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.deep.DeepBoltzmannMachineTrainer;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.training.TrainingBatchCompletedListener;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;
import dbn.BackpropagationDeepBeliefNetworkTrainer;
import dbn.DeepBeliefNetwork;

public class Main {
	private static final String PATH = "E:\\Dropbox\\mgr-asr";
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_experiments";

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		args = new String[] { "gauss" };
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
			case "gauss":
				testGaussian();
				return;
			case "test":
				// MfccFileParser p = new MfccFileParser(PATH);
				// allPhones = p.getPhones(new File(PATH + "\\phones"), true);
				// List<InputStateVector> training = ObjectIOManager.load(new
				// File("_training"));
				// testPhoneClassification(training, allPhones);
				return;
			case "newtest":
				List<InputStateVector> trainingSet = ObjectIOManager.load(new File("_training"));
				// List<InputStateVector> testSet = ObjectIOManager.load(new
				// File("_training"));
				List<InputStateVector> tests = new ArrayList<>();

				Set<String> trainingPhones = new HashSet<>();
				for (InputStateVector vector : trainingSet) {
					trainingPhones.add(vector.getLabel());
				}

				System.out.println(trainingPhones + " " + trainingPhones.size());

				for (String phone : trainingPhones) {
					int i = 0;
					int j = 0;
					while (i < trainingSet.size()) {
						InputStateVector vec = trainingSet.get(i);
						if (vec.getLabel().equals(phone)) {
							tests.add(vec);
							j++;
						}
						if (j == 50) {
							break;
						}
						i++;
					}
				}
				testPhoneClassification(tests, new ArrayList<String>(trainingPhones));
				return;
			}
		} else {
			// testRestrictedBoltzmannMachine();
			// testDeepBoltzmannMachine();
			// testDeepBeliefNetwork();
			// testClassification();
			// testGaussian();
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
				List<InputStateVector> training = ObjectIOManager.load(new File("_training"));

				/**
				 * DeepBoltzmannMachine dbm = ObjectIOManager.load(list[0]);
				 * dbm.createThreadManager(); File learningF = new
				 * File(SAVE_PATH + "\\learning.factor"); AdaptiveLearningFactor
				 * lf; if (learningF.exists()) { lf = ObjectIOManager.load(new
				 * File("learning.factor")); } else { lf = new
				 * AdaptiveLearningFactor(0.0001, 0.8, 1.5); }
				 */
				// DeepBoltzmannMachineTrainer trainer = new
				// DeepBoltzmannMachineTrainer(dbm, lf, 100, Double.MIN_VALUE);
				// String[] split = stats.split("_");
				// int lastLayer = Integer.valueOf(split[0]);
				// int lastEpoch = Integer.valueOf(split[2]);
				// trainer.setStartLayer(lastLayer);
				// trainer.setStart(lastEpoch + 1);
				// trainer.train(training);

				MfccFileParser p = new MfccFileParser(PATH);
				List<String> phones = p.getPhones(new File(PATH + "\\phones"), true);

				final DeepBeliefNetwork dbn = ObjectIOManager.load(list[0]);
				String[] split = stats.split("_");
				System.out.println(Arrays.toString(split));
				int start = Integer.valueOf(split[1]);
				// final DeepBeliefNetwork dbn =
				// BoltzmannMachineFactory.getDeepBeliefNetwork(dbm,
				// LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(),
				// phones.size());
				BackpropagationDeepBeliefNetworkTrainer backPropTrainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, new AdaptiveLearningFactor(0.02, 1, 1), 25, 0.9);
				backPropTrainer.addTrainingBatchCompletedListener(new TrainingBatchCompletedListener() {

					@Override
					public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
						System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
					}
				});
				backPropTrainer.setStart(start + 1);
				backPropTrainer.train(training);

				// TODO
				// testPhoneClassification(training, phones);

			} else {
				MfccFileParser p = new MfccFileParser(PATH);
				p.setRemoveSilence(true);
				List<MfccSample> samples = p.loadSamples();
				List<InputStateVector> data = new ArrayList<>();
				for (MfccSample sample : samples) {
					for (PhoneTiming timing : sample.getVectors()) {
						data.add(timing.getMfccs());
					}
				}
				TrainingSetGenerator generator = new TrainingSetGenerator(1000);
				generator.splitData(data);
				List<InputStateVector> training = generator.getTrainingSet();
				List<InputStateVector> testing = generator.getTestingSet();

				GaussianDistributionDataNormalizer normalizer = new GaussianDistributionDataNormalizer();
				normalizer.normalizeTrainingSet(training);
				System.out.println(normalizer.getMean() + " " + normalizer.getStdev());

				ObjectIOManager.save(normalizer, new File("_normalizer"));
				ObjectIOManager.save(training, new File("_training"));
				ObjectIOManager.save(testing, new File("_testing"));

				Integer[] layers = new Integer[] { MfccParams.VECTOR_SIZE, 3072, 3072, 3072, 3072, 3072 };
				DeepBoltzmannMachine dbm = BoltzmannMachineFactory.getDeepBotlzmannMachine(true, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), layers);
				dbm.createThreadManager();
				DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, new AdaptiveLearningFactor(0.001, 0.8, 1.5), 100, Double.MIN_VALUE, 0.9);
				trainer.train(training);
			}
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
		List<InputStateVector> training = reader.getTrainingSetItems(true);
		final int numVisible = reader.getCols() * reader.getRows();
		final int numHidden = 10 * 10;

		final List<MNISTDigitElement> tests = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			MNISTDigitElement test = reader.getTestItem(i);
			test.binarize();
			tests.add(test);
		}

		final RestrictedBoltzmannMachine rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(false, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), numVisible, numHidden);
		RestrictedBoltzmannMachineTrainer t = new RestrictedBoltzmannMachineTrainer(rbm, new AdaptiveLearningFactor(), 500, Double.MIN_VALUE, 0.9);
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
						output[j] = (int) (vis[j] * 255);
						output[j] = new Color(output[j], output[j], output[j]).getRGB();
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

	private static void testGaussian() {
		final MnistPanel p = new MnistPanel();
		JFrame f = new JFrame();
		f.setContentPane(p);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(100);
		}
		List<InputStateVector> training = reader.getTrainingSetItems(false);
		final int numVisible = reader.getCols() * reader.getRows();
		final int numHidden = 10 * 10;

		final List<MNISTDigitElement> tests = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			MNISTDigitElement test = reader.getTestItem(i);
			for (int j = 0; j < test.size(); j++) {
				test.set(i, test.get(i) / 255);
			}
			tests.add(test);
		}

		for (InputStateVector vector : training) {
			for (int i = 0; i < vector.size(); i++) {
				vector.set(i, vector.get(i) / 255);
			}
		}

		final GaussianDistributionDataNormalizer normalizer = new GaussianDistributionDataNormalizer();
		normalizer.normalizeTrainingSet(training);

		System.out.println(normalizer.getMean() + " " + normalizer.getStdev());

		final RestrictedBoltzmannMachine rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(true, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), numVisible, numHidden);
		RestrictedBoltzmannMachineTrainer t = new RestrictedBoltzmannMachineTrainer(rbm, new AdaptiveLearningFactor(0.001, 1, 1), 500, Double.MIN_VALUE, 0.9);
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
					for (int j = 0; j < test.size(); j++) {
						test.set(j, (test.get(j) - normalizer.getMean()) / normalizer.getStdev());
					}
					rbm.resetStates();
					rbm.initializeVisibleLayerStates(test);
					rbm.updateHiddenUnits();
					rbm.resetVisibleStates();
					rbm.reconstructVisibleUnits();
					rbm.reconstructHiddenUnits();
					double[] vis = rbm.getVisibleLayerStates();
					int[] output = new int[vis.length];
					for (int j = 0; j < vis.length; j++) {
						double d = (vis[j] + normalizer.getMean()) * normalizer.getStdev();
						output[j] = (int) Math.round(d);
						if (output[j] < 0) {
							output[j] = 0;
						}
						if (output[j] > 255) {
							output[j] = 255;
						}
						output[j] = new Color(output[j], output[j], output[j]).getRGB();
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
		List<InputStateVector> training = reader.getTrainingSetItems(true);
		DeepBoltzmannMachine dbm = BoltzmannMachineFactory.getDeepBotlzmannMachine(false, LayerConnectorWeightInitializerFactory.getGaussianWeightInitializer(), 784, 1000, 1000, 1000);
		DeepBoltzmannMachineTrainer trainer = new DeepBoltzmannMachineTrainer(dbm, new AdaptiveLearningFactor(0.01, 1, 1), 100, Double.MIN_VALUE, 0.9);
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
		List<InputStateVector> training = reader.getTrainingSetItems(true);
		try {
			DeepBoltzmannMachine dbm = ObjectIOManager.load(new File("E:\\Dropbox\\rbm test\\deep_boltzmann.dbm"));
			dbm.createThreadManager();
			DeepBeliefNetwork dbn = BoltzmannMachineFactory.getDeepBeliefNetwork(dbm, LayerConnectorWeightInitializerFactory.getZeroWeightInitializer(), 10);
			BackpropagationDeepBeliefNetworkTrainer trainer = new BackpropagationDeepBeliefNetworkTrainer(dbn, new AdaptiveLearningFactor(0.2, 1, 1), 25, 0.9);
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
		List<InputStateVector> testing = reader.getTestingSetItems(true);
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

	private static void testPhoneClassification(final List<InputStateVector> training, final List<String> phones) throws Exception {
		Map<String, List<InputStateVector>> trainingClasses = new HashMap<>();
		for (int i = 0; i < training.size(); i++) {
			InputStateVector vector = training.get(0);
			String label = vector.getLabel();
			List<InputStateVector> vectorList = trainingClasses.get(label);
			if (vectorList == null) {
				vectorList = new ArrayList<>();
			}
			vectorList.add(vector);
			trainingClasses.put(label, vectorList);
		}
		final Map<String, ConfusionMatrix> classes = new HashMap<>();
		for (String phone : phones) {
			classes.put(phone, new ConfusionMatrix());
		}

		int procs = Runtime.getRuntime().availableProcessors();
		List<List<String>> procSplit = new ArrayList<>();
		int split = phones.size() / procs;

		System.out.println("split " + split);
		int i;
		for (i = 0; i < phones.size(); i += split) {
			ArrayList<String> states = new ArrayList<>();
			for (int j = i; j < i + split && j < phones.size(); j++) {
				states.add(phones.get(j));
			}
			procSplit.add(states);
		}
		while (i < procs * split) {
			procSplit.get(procs - 1).add(phones.get(i));
		}

		List<Callable<Double[]>> tasks = new ArrayList<>();
		for (final List<String> procPhones : procSplit) {
			tasks.add(new Callable<Double[]>() {
				@Override
				public Double[] call() throws Exception {
					DeepBeliefNetwork dbn = ObjectIOManager.load(new File("E:\\Dropbox\\mfcc_expermients\\backprop_17.dbn"));
					MfccFileParser pars = new MfccFileParser(PATH);
					List<String> allPhones = pars.getPhones(new File(PATH + "\\phones"), true);
					return classify(classes, new ArrayList<>(training), dbn, procPhones, allPhones);
				}
			});

		}
		ExecutorService exec = Executors.newFixedThreadPool(procs);
		List<Future<Double[]>> eList = exec.invokeAll(tasks);
		double acc = 0;
		double prec = 0;
		double recall = 0;
		double f = 0;
		for (Future<Double[]> d : eList) {
			Double[] val = d.get();
			acc += val[0];
			prec += val[1];
			recall += val[2];
			f += val[3];
		}
		exec.shutdown();

		File output = new File("results-all.txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		bw.write("all");
		bw.newLine();
		bw.write("Accuracy: ");
		bw.write("" + acc / phones.size());
		bw.newLine();
		bw.write("Precision: ");
		bw.write("" + prec / phones.size());
		bw.newLine();
		bw.write("Recall: ");
		bw.write("" + recall / phones.size());
		bw.newLine();
		bw.write("FScore: ");
		bw.write("" + f / phones.size());
		bw.newLine();
		bw.flush();
		bw.close();
	}

	private static Double[] classify(Map<String, ConfusionMatrix> classes, List<InputStateVector> training, DeepBeliefNetwork dbn, List<String> phones, List<String> allPhones) throws Exception {
		double acc = 0;
		double prec = 0;
		double recall = 0;
		double f = 0;
		for (String positivePhone : phones) {
			ConfusionMatrix matrix = classes.get(positivePhone);
			for (InputStateVector vector : training) {
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
				String classifiedPhone = allPhones.get(maxIndex);
				String realPhone = vector.getLabel();
				System.out.println("positive: " + changePhone(positivePhone) + " " + training.indexOf(vector));
				System.out.println("chose: " + changePhone(classifiedPhone) + " real: " + changePhone(realPhone) + " index: " + maxIndex);
				boolean reallyPositive = realPhone.equals(positivePhone);
				boolean classifiedPositive = classifiedPhone.equals(positivePhone);
				if (reallyPositive && classifiedPositive) {
					matrix.increaseTruePositive();
				} else if (reallyPositive) {
					matrix.increaseFalseNegative();
				} else if (classifiedPositive) {
					matrix.increaseFalsePositive();
				} else {
					matrix.increaseTrueNegative();
				}
			}

			File output = new File("results-" + UUID.randomUUID() + ".txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write(positivePhone);
			bw.newLine();
			bw.write("Accuracy: ");
			bw.write("" + matrix.getAccuracy());
			acc += matrix.getAccuracy();
			bw.newLine();
			bw.write("Precision: ");
			bw.write("" + matrix.getPrecision());
			prec += matrix.getPrecision();
			bw.newLine();
			bw.write("Recall: ");
			bw.write("" + matrix.getRecall());
			recall += matrix.getRecall();
			bw.newLine();
			bw.write("FScore: ");
			bw.write("" + matrix.getFScore());
			f += matrix.getFScore();
			bw.flush();
			bw.close();
		}
		return new Double[] { acc, prec, recall, f };
	}

	private static String changePhone(String phone) {
		if (phone.contains("\305\233"))
			return "ú";
		if (phone.contains("\304\207"))
			return "Ê";
		if (phone.contains("\305\204"))
			return "Ò";
		if (phone.contains("d\305\272"))
			return "dü";
		if (phone.contains("\305\272"))
			return "ü";
		if (phone.contains("\305\202"))
			return "≥";
		if (phone.contains("d\305\274"))
			return "dø";
		if (phone.contains("\305\274"))
			return "ø";
		phone.replaceAll("~", "-");
		System.out.println("nope");
		return phone;
	}
}
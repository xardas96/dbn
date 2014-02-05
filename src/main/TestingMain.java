package main;

import io.ObjectIOManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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

import testsets.mfcc.MfccFileParser;
import utils.ConfusionMatrix;
import boltzmann.layers.Layer;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;
import dbn.DeepBeliefNetwork;

public class TestingMain {
	private static final String PATH = "E:\\Dropbox\\mgr-asr";
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_new";
	private static final String DBN_NAME = "backprop_24.dbn";

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		List<InputStateVector> trainingSet = ObjectIOManager.load(new File("training.data"));
		Set<String> trainingPhones = new HashSet<>();
		for (InputStateVector vector : trainingSet) {
			trainingPhones.add(vector.getLabel());
		}
		List<InputStateVector> trainings = new ArrayList<>();
		for (String phone : trainingPhones) {
			int i = 0;
			int j = 0;
			while (i < trainingSet.size()) {
				InputStateVector vec = trainingSet.get(i);
				if (vec.getLabel().equals(phone)) {
					trainings.add(vec);
					j++;
				}
				if (j == 100) {
					break;
				}
				i++;
			}
		}
		testPhoneClassification(trainings, new ArrayList<String>(trainingPhones), DBN_NAME, "training");

		List<InputStateVector> testSet = ObjectIOManager.load(new File("testing.data"));
		Set<String> testingPhones = new HashSet<>();
		for (InputStateVector vector : testSet) {
			testingPhones.add(vector.getLabel());
		}
		List<InputStateVector> tests = new ArrayList<>();
		for (String phone : trainingPhones) {
			int i = 0;
			int j = 0;
			while (i < testSet.size()) {
				InputStateVector vec = testSet.get(i);
				if (vec.getLabel().equals(phone)) {
					tests.add(vec);
					j++;
				}
				if (j == 100) {
					break;
				}
				i++;
			}
		}
		testPhoneClassification(tests, new ArrayList<String>(testingPhones), DBN_NAME, "testing");
	}

	private static void testPhoneClassification(final List<InputStateVector> training, final List<String> phones, final String dbnFileName, final String outputDir) throws Exception {
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
					DeepBeliefNetwork dbn = ObjectIOManager.load(new File(PATH + "\\" + dbnFileName));
					MfccFileParser pars = new MfccFileParser(PATH);
					List<String> allPhones = pars.getPhones(new File(PATH + "\\phones"), true);
					return classify(classes, new ArrayList<>(training), dbn, procPhones, allPhones, outputDir);
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

		File dir = new File(SAVE_PATH + "\\" + outputDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File output = new File(SAVE_PATH + "\\" + outputDir + "\\results-all.txt");
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

	private static Double[] classify(Map<String, ConfusionMatrix> classes, List<InputStateVector> training, DeepBeliefNetwork dbn, List<String> phones, List<String> allPhones, String outputDir) throws Exception {
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
			File dir = new File(SAVE_PATH + "\\" + outputDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File output = new File(SAVE_PATH + "\\" + outputDir + "\\results-" + UUID.randomUUID() + ".txt");
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
		return phone;
	}
}
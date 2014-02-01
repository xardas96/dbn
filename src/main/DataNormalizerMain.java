package main;

import io.ObjectIOManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import testsets.mfcc.MfccFileParser;
import testsets.mfcc.MfccSample;
import testsets.mfcc.PhoneTiming;
import utils.ClassDistributionCounter;
import utils.GaussianDistributionDataNormalizer;
import utils.TrainingSetGenerator;
import boltzmann.vectors.InputStateVector;

public class DataNormalizerMain {
	private static final String PATH = "E:\\Dropbox\\mgr-asr";
	private static final String SAVE_PATH = "E:\\Dropbox\\mfcc_new";

	public static void main(String[] args) throws Exception {
		ObjectIOManager.setSavePath(SAVE_PATH);
		ObjectIOManager.setLoadPath(SAVE_PATH);
		MfccFileParser p = new MfccFileParser(PATH);
		p.setRemoveSilence(true);
		List<MfccSample> samples = p.loadSamples();
		List<InputStateVector> data = new ArrayList<>();
		for (MfccSample sample : samples) {
			for (PhoneTiming timing : sample.getVectors()) {
				data.add(timing.getMfccs());
			}
		}
		
		saveClassDistribution(data, SAVE_PATH + "\\all-distribution.txt");

		GaussianDistributionDataNormalizer normalizer = new GaussianDistributionDataNormalizer();
		normalizer.normalizeTrainingSet(data);
		System.out.println("mean: " + normalizer.getMean() + ", stdev: " + normalizer.getStdev());

		TrainingSetGenerator generator = new TrainingSetGenerator(900);
		generator.splitData(data);
		List<InputStateVector> training = generator.getTrainingSet();
		List<InputStateVector> testing = generator.getTestingSet();

		saveClassDistribution(training, SAVE_PATH + "\\training-distribution.txt");
		saveClassDistribution(testing, SAVE_PATH + "\\testing-distribution.txt");

		System.out.println("training size: " + training.size() + ", testing size: " + testing.size());

		ObjectIOManager.save(normalizer, new File("normalizer.gauss"));
		ObjectIOManager.save(training, new File("training.data"));
		ObjectIOManager.save(testing, new File("testing.data"));
	}

	private static void saveClassDistribution(List<InputStateVector> data, String fileName) throws Exception {
		Map<String, Integer> counts = ClassDistributionCounter.generateClassDistribution(data);
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for (String lab : counts.keySet()) {
			bw.write(lab + " " + counts.get(lab));
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}
}
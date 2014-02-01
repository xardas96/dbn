package utils;

import java.io.Serializable;
import java.util.List;

import boltzmann.vectors.InputStateVector;

public class GaussianDistributionDataNormalizer implements Serializable {
	private static final long serialVersionUID = 617004861758885042L;
	private double mean;
	private double stdev;

	public void normalizeTrainingSet(List<InputStateVector> training) {
		calculateMean(training);
		calculateStdev(training);
		for (InputStateVector vector : training) {
			for (int i = 0; i < vector.size(); i++) {
				vector.set(i, (vector.get(i) - mean) / stdev);
			}
		}
	}

	public void normalizeVector(InputStateVector vector) {
		for (int j = 0; j < vector.size(); j++) {
			vector.set(j, (vector.get(j) - mean) / stdev);
		}
	}

	public double getMean() {
		return mean;
	}

	public double getStdev() {
		return stdev;
	}

	private void calculateMean(List<InputStateVector> data) {
		for (InputStateVector vector : data) {
			for (int i = 0; i < vector.size(); i++) {
				mean += vector.get(i);
			}
		}
		mean /= data.size() * data.get(0).size();
	}

	private void calculateStdev(List<InputStateVector> data) {
		for (InputStateVector vector : data) {
			for (int i = 0; i < vector.size(); i++) {
				stdev += (mean - vector.get(i)) * (mean - vector.get(i));
			}
		}
		stdev /= data.size() * data.get(0).size();
		stdev = Math.sqrt(stdev);
	}
}
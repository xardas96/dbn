package boltzmann.training;

public class AdaptiveLearningFactor {
	private double learningFactor;
	private double rhoD;
	private double rhoI;
	private int maxLearningFactorCounter;

	public AdaptiveLearningFactor() {
		this.learningFactor = 0.01;
		this.rhoD = 0.8;
		this.rhoI = 1.5;
		this.maxLearningFactorCounter = 0;
	}

	public AdaptiveLearningFactor(double learningFactor, double rhoD, double rhoI) {
		this.learningFactor = learningFactor;
		this.rhoD = rhoD;
		this.rhoI = rhoI;
		this.maxLearningFactorCounter = 0;
	}

	public void updateLearningFactor(double previousError, double currentError) {
		if (previousError != 0.0) {
			if (currentError > previousError) {
				decreaseLearningFactor();
			} else if (previousError - currentError < 0.01) {
				increaseLearningFactor();
			}
			if (learningFactor == 1.0) {
				maxLearningFactorCounter++;
			} else {
				maxLearningFactorCounter = 0;
			}
			if (maxLearningFactorCounter == 2000) {
				maxLearningFactorCounter = 0;
				learningFactor = 0.2;
			}
		}
	}

	public double getLearningFactor() {
		return learningFactor;
	}

	private void decreaseLearningFactor() {
		learningFactor *= rhoD;
		if (learningFactor >= 1.0) {
			learningFactor = 1.0;
		}
	}

	private void increaseLearningFactor() {
		learningFactor *= rhoI;
		if (learningFactor >= 1.0) {
			learningFactor = 1.0;
		}
	}
}
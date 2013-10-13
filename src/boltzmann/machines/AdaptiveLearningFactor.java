package boltzmann.machines;

public class AdaptiveLearningFactor {
	private float learningFactor;
	private float rhoD;
	private float rhoI;
	private int maxLearningFactorCounter;

	public AdaptiveLearningFactor() {
		this.learningFactor = 0.2f;
		this.rhoD = 0.8f;
		this.rhoI = 1.5f;
		this.maxLearningFactorCounter = 0;
	}

	public AdaptiveLearningFactor(float learningFactor, float rhoD, float rhoI) {
		this.learningFactor = learningFactor;
		this.rhoD = rhoD;
		this.rhoI = rhoI;
	}

	public void updateLearningFactor(float previousError, float currentError) {
		if (previousError != 0.0f) {
			if (currentError > previousError) {
				decreaseLearningFactor();
			} else if (previousError - currentError < 0.01) {
				increaseLearningFactor();
			}
			if (learningFactor == 1.0f) {
				maxLearningFactorCounter++;
			} else {
				maxLearningFactorCounter = 0;
			}
			if (maxLearningFactorCounter == 2000) {
				maxLearningFactorCounter = 0;
				learningFactor = 0.2f;
			}
		}
	}

	public float getLearningFactor() {
		return learningFactor;
	}

	private void decreaseLearningFactor() {
		learningFactor *= rhoD;
		if (learningFactor >= 1.0f) {
			learningFactor = 1.0f;
		}
	}

	private void increaseLearningFactor() {
		learningFactor *= rhoI;
		if (learningFactor >= 1.0f) {
			learningFactor = 1.0f;
		}
	}
}
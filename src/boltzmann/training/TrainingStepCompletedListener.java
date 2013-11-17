package boltzmann.training;

public interface TrainingStepCompletedListener {
	public void onTrainingStepComplete(int step, int maxSteps);
	public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor);
}
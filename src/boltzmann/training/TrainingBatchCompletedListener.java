package boltzmann.training;

public interface TrainingBatchCompletedListener {
	public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor);
}
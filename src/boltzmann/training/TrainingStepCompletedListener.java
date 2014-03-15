package boltzmann.training;

public interface TrainingStepCompletedListener extends TrainingBatchCompletedListener {
	public void onTrainingStepComplete(int step, int maxSteps, int cdK);
}
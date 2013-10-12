package boltzmann.machines;

public interface TrainingStepCompletedListener {
	public void onTrainingStepComplete(int step, int maxSteps);
	public void onTrainingBatchComplete(int currentEpoch, float currentError);
}
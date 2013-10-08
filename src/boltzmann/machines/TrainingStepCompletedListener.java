package boltzmann.machines;

public interface TrainingStepCompletedListener {
	public void onTrainingStepComplete();
	public void onTrainingBatchComplete(int currentEpoch, float currentError);
}
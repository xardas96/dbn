package boltzmann.machines;

import java.util.Collections;
import java.util.List;

import boltzmann.vectors.InputStateVector;

public abstract class BoltzmannMachineTrainer<B extends BoltzmannMachine> {
	protected B bm;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private float maxError;
	private TrainingStepCompletedListener trainingStepCompletedListener;
	private float error;
	private float previousError;

	public BoltzmannMachineTrainer(B bm, AdaptiveLearningFactor learningFactor, int maxEpochs, float maxError) {
		this.bm = bm;
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
	}

	public void setTrainingStepCompletedListener(TrainingStepCompletedListener trainingStepCompletedListener) {
		this.trainingStepCompletedListener = trainingStepCompletedListener;
	}

	protected abstract void train(InputStateVector trainingVector, int trainingVectorSize, float learningFactor);

	protected abstract float calculateErrorDelta(InputStateVector trainingVector);

	public void train(List<InputStateVector> trainingVectors) {
		error = Float.MAX_VALUE;
		previousError = 0;
		int trainigBatchSize = trainingVectors.size();
		for (int i = 0; i < maxEpochs && error > maxError; i++) {
			Collections.shuffle(trainingVectors);
			error = 0;
			for (int j = 0; j < trainigBatchSize; j++) {
				InputStateVector vector = trainingVectors.get(j);
				train(vector, trainigBatchSize, learningFactor.getLearningFactor());
				error += calculateErrorDelta(vector);
				trainingStepCompletedListener.onTrainingStepComplete(j, trainigBatchSize);
				bm.resetUnitStates();
			}
			error /= trainingVectors.size();
			learningFactor.updateLearningFactor(previousError, error);
			trainingStepCompletedListener.onTrainingBatchComplete(i, error, learningFactor.getLearningFactor());
			previousError = error;
		}
	}
}
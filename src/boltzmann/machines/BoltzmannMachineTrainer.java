package boltzmann.machines;

import java.util.Collections;
import java.util.List;

import boltzmann.vectors.InputStateVector;

public abstract class BoltzmannMachineTrainer<B extends BoltzmannMachine> {
	protected B bm;
	private int maxEpochs;
	private float maxError;
	private TrainingStepCompletedListener trainingStepCompletedListener;

	public BoltzmannMachineTrainer(B bm, float learningRate, int maxEpochs, float maxError) {
		this.bm = bm;
		this.bm.setLearningRate(learningRate);
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
	}

	public void setTrainingStepCompletedListener(TrainingStepCompletedListener trainingStepCompletedListener) {
		this.trainingStepCompletedListener = trainingStepCompletedListener;
	}

	protected abstract void train(InputStateVector trainingVector, int trainingVectorSize);

	protected abstract float calculateErrorDelta(InputStateVector trainingVector);

	public void train(List<InputStateVector> trainingVectors) {
		float error = Float.MAX_VALUE;
		int trainigBatchSize = trainingVectors.size();
		for (int i = 0; i < maxEpochs && error > maxError; i++) {
			Collections.shuffle(trainingVectors);
			error = 0;
			for (int j = 0; j < trainigBatchSize; j++) {
				InputStateVector vector = trainingVectors.get(j);
				train(vector, trainingVectors.size());
				error += calculateErrorDelta(vector);
				trainingStepCompletedListener.onTrainingStepComplete(j, trainigBatchSize);
				bm.resetUnitStates();
			}
			error /= trainingVectors.size();
			trainingStepCompletedListener.onTrainingBatchComplete(i, error);
		}
	}
}
package boltzmann.machines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.training.Trainer;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.vectors.InputStateVector;

public abstract class BoltzmannMachineTrainer<B extends BoltzmannMachine> implements Trainer {
	protected B bm;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private double maxError;
	private List<TrainingStepCompletedListener> trainingStepCompletedListeners;
	private double error;
	private double previousError;

	public BoltzmannMachineTrainer(B bm, AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError) {
		this.bm = bm;
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
		this.trainingStepCompletedListeners = new ArrayList<>();
	}
	
	public void setBm(B bm) {
		this.bm = bm;
	}

	public void addTrainingStepCompletedListener(TrainingStepCompletedListener trainingStepCompletedListener) {
		trainingStepCompletedListeners.add(trainingStepCompletedListener);
	}

	protected abstract void train(InputStateVector trainingVector, int trainingVectorSize, double learningFactor);

	protected abstract double calculateErrorDelta(InputStateVector trainingVector);

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		error = Double.MAX_VALUE;
		previousError = 0;
		int trainigBatchSize = trainingVectors.size();
		for (int i = 0; i < maxEpochs && error > maxError; i++) {
			Collections.shuffle(trainingVectors);
			error = 0;
			for (int j = 0; j < trainigBatchSize; j++) {
				InputStateVector vector = trainingVectors.get(j);
				train(vector, trainigBatchSize, learningFactor.getLearningFactor());
				error += calculateErrorDelta(vector);
				if (!trainingStepCompletedListeners.isEmpty()) {
					for (TrainingStepCompletedListener trainingStepCompletedListener : trainingStepCompletedListeners) {
						trainingStepCompletedListener.onTrainingStepComplete(j, trainigBatchSize);
					}
				}
				bm.resetStates();
			}
			error /= trainingVectors.size();
			learningFactor.updateLearningFactor(previousError, error);
			if (!trainingStepCompletedListeners.isEmpty()) {
				for (TrainingStepCompletedListener trainingStepCompletedListener : trainingStepCompletedListeners) {
					trainingStepCompletedListener.onTrainingBatchComplete(i, error, learningFactor.getLearningFactor());
				}
			}
			previousError = error;
		}
	}
}
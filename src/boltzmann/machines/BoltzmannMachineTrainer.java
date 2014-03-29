package boltzmann.machines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boltzmann.training.Trainer;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.vectors.InputStateVector;

public abstract class BoltzmannMachineTrainer<B extends BoltzmannMachine> implements Trainer {
	protected B bm;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private double maxError;
	private List<TrainingStepCompletedListener> trainingStepCompletedListeners;
	private double error;
	private double previousError;
	private int start;
	private double initialMomentum;
	private double finalMomentum;
	private double momentum;
	private int k;
	private double dropOutProbability;
	private static final int MOMENTUM_SWITCH_EPOCH = 5;

	public BoltzmannMachineTrainer(B bm, AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError, double initialMomentum, double finalMomentum, int k, double dropOutProbability) {
		this.bm = bm;
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
		this.trainingStepCompletedListeners = new ArrayList<>();
		this.initialMomentum = initialMomentum;
		this.finalMomentum = finalMomentum;
		this.k = k;
		this.dropOutProbability = dropOutProbability;
	}

	public void setBm(B bm) {
		this.bm = bm;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void addTrainingStepCompletedListener(TrainingStepCompletedListener trainingStepCompletedListener) {
		trainingStepCompletedListeners.add(trainingStepCompletedListener);
	}

	public void setLearningFactor(AdaptiveLearningFactor learningFactor) {
		this.learningFactor = learningFactor;
	}

	public AdaptiveLearningFactor getLearningFactor() {
		return learningFactor;
	}

	protected abstract void train(InputStateVector trainingVector, int trainingVectorSize, double learningFactor, double momentum, double dropOutProbability);

	protected abstract double calculateErrorDelta(InputStateVector trainingVector);

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		error = Double.MAX_VALUE;
		previousError = 0;
		int trainigBatchSize = trainingVectors.size();
		for (int i = start; i < maxEpochs && error > maxError; i++) {
			if (i > MOMENTUM_SWITCH_EPOCH) {
				momentum = finalMomentum;
			} else {
				momentum = initialMomentum;
			}
			Collections.shuffle(trainingVectors);
			error = 0;
			for (int j = 0; j < trainigBatchSize; j++) {
				InputStateVector vector = trainingVectors.get(j);
				for (int cd = 0; cd < k; cd++) {
					train(vector, trainigBatchSize, learningFactor.getLearningFactor(), momentum, dropOutProbability);
					error += calculateErrorDelta(vector);
					for (TrainingStepCompletedListener trainingStepCompletedListener : trainingStepCompletedListeners) {
						trainingStepCompletedListener.onTrainingStepComplete(j, trainigBatchSize, cd);
					}
					bm.resetStates();
				}
			}
			error /= trainingVectors.size();
			learningFactor.updateLearningFactor(previousError, error);
			for (TrainingStepCompletedListener trainingStepCompletedListener : trainingStepCompletedListeners) {
				trainingStepCompletedListener.onTrainingBatchComplete(i, error, learningFactor.getLearningFactor());
			}
			previousError = error;
		}
	}
}
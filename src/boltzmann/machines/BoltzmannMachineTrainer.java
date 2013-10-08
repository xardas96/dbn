package boltzmann.machines;

import java.util.Collections;
import java.util.List;

import boltzmann.vectors.InputStateVector;

public abstract class BoltzmannMachineTrainer<B extends BoltzmannMachine> {
	protected B bm;
	private int maxEpochs;
	private float maxError;

	public BoltzmannMachineTrainer(B bm, int maxEpochs, float maxError) {
		this.bm = bm;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
	}

	protected abstract void train(InputStateVector trainingVector, int trainingVectorSize);

	protected abstract float calculateErrorDelta(InputStateVector trainingVector);

	public void train(List<InputStateVector> trainingVectors) {
		float error = Float.MAX_VALUE;
		for (int i = 0; i < maxEpochs && error > maxError; i++) {
			Collections.shuffle(trainingVectors);
			error = 0;
			for (int j = 0; j < trainingVectors.size(); j++) {
				InputStateVector vector = trainingVectors.get(j);
				train(vector, trainingVectors.size());
				error += calculateErrorDelta(vector);
			}
			error /= trainingVectors.size();
			System.out.println("Epoch: " + i + ", error: " + error);
		}
	}
}
package boltzmann.training;

import java.util.List;

import boltzmann.vectors.InputStateVector;

public interface Trainer {
	public void train(List<InputStateVector> trainingVectors);
}
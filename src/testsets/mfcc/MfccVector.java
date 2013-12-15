package testsets.mfcc;

import boltzmann.vectors.InputStateVector;

public class MfccVector extends InputStateVector {

	public void setInputStates(Double[] inputStates) {
		double[] array = new double[inputStates.length];
		for (int i = 0; i < inputStates.length; i++) {
			array[i] = inputStates[i];
		}
		super.setInputStates(array);
	}
}
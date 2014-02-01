package testsets.mfcc;

import boltzmann.vectors.InputStateVector;

public class MfccFrameVector extends InputStateVector {
	private static final long serialVersionUID = 2616046128597447968L;

	public void setInputStates(Double[] inputStates) {
		double[] array = new double[inputStates.length];
		for (int i = 0; i < inputStates.length; i++) {
			array[i] = inputStates[i] == null ? 0 : inputStates[i];
		}
		super.setInputStates(array);
	}
}
package mnist;

import boltzmann.vectors.InputStateVector;

public class MNISTDigitElement extends InputStateVector {

	public MNISTDigitElement(double[] inputStates) {
		super(inputStates);
	}

	public void binarize() {
		for (int i = 0; i < size(); i++) {
			set(i, get(i) == 0 ? 0 : 1);
		}
	}

	public void setLabel(int label) {
		super.setLabel(String.valueOf(label));
	}
}
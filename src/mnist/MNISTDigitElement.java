package mnist;

import boltzmann.vectors.InputStateVector;

public class MNISTDigitElement extends InputStateVector {
	private int label;

	public MNISTDigitElement(float[] inputStates) {
		super(inputStates);
	}

	public void binarize() {
		for (int i = 0; i < size(); i++) {
			set(i, get(i) == 0 ? 0 : 1);
		}
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}
}
package boltzmann.vectors;

public class InputStateVector {
	private float[] inputStates;

	public InputStateVector() {
	}
	
	public InputStateVector(float[] inputStates) {
		this.inputStates = inputStates;
	}
	
	public void setInputStates(float[] inputStates) {
		this.inputStates = inputStates;
	}
	
	public float[] getInputStates() {
		return inputStates;
	}

	public float get(int index) {
		return inputStates[index];
	}
	
	public void set(int index, float value) {
		inputStates[index] = value;
	}
	
	public int size() {
		return inputStates.length;
	}
	
	@Override
	public String toString() {
		return inputStates.toString();
	}
}
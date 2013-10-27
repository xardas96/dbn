package boltzmann.vectors;

public class InputStateVector {
	private float[] inputStates;
	private String label;

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
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return label + ": " + inputStates.toString();
	}
}
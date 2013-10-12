package boltzmann.vectors;

public class InputStateVector {
	private int[] inputStates;

	public InputStateVector(int[] inputStates) {
		this.inputStates = inputStates;
	}

	public int get(int index) {
		return inputStates[index];
	}
	
	public void set(int index, int value) {
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
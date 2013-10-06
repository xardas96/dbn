package boltzmann.vectors;

public class InputStateVector {
	private int[] inputStates;

	public InputStateVector(int[] inputStates) {
		this.inputStates = inputStates;
	}

	public int get(int index) {
		return inputStates[index];
	}
	
	public int size() {
		return inputStates.length;
	}
}
package boltzmann.vectors;

public class InputStateVector {
	private double[] inputStates;
	private double[] outputStates;
	private String label;

	public InputStateVector() {
	}

	public InputStateVector(double[] inputStates) {
		this.inputStates = inputStates;
	}

	public void setInputStates(double[] inputStates) {
		this.inputStates = inputStates;
	}
	
	public double[] getInputStates() {
		return inputStates;
	}

	public void setOutputStates(double[] outputStates) {
		this.outputStates = outputStates;
	}

	public double[] getOutputStates() {
		return outputStates;
	}

	public double get(int index) {
		return inputStates[index];
	}

	public void set(int index, double value) {
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
package boltzmann.vectors;

import java.io.Serializable;
import java.util.Arrays;

public class InputStateVector implements Serializable {
	private static final long serialVersionUID = 3974313776261299667L;
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
		return label + ": " + Arrays.toString(inputStates);
	}
}
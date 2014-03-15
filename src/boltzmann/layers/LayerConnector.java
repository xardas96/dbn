package boltzmann.layers;

import java.io.Serializable;

public class LayerConnector implements Serializable {
	private static final long serialVersionUID = -117908422331865881L;
	private Layer bottomLayer;
	private Layer topLayer;
	private double[][] unitConnectionWeights;
	private double[][] weightSteps;

	public LayerConnector(Layer bottomLayer, Layer topLayer, LayerConnectorWeightInitializer weightInitializer) {
		this.bottomLayer = bottomLayer;
		this.topLayer = topLayer;
		unitConnectionWeights = new double[bottomLayer.size()][topLayer.size()];
		for (int i = 0; i < unitConnectionWeights.length; i++) {
			for (int j = 0; j < unitConnectionWeights[i].length; j++) {
				unitConnectionWeights[i][j] = weightInitializer.getWeight();
			}
		}
		weightSteps = new double[bottomLayer.size()][topLayer.size()];
		for (int i = 0; i < weightSteps.length; i++) {
			for (int j = 0; j < weightSteps[i].length; j++) {
				weightSteps[i][j] = 0;
			}
		}
	}

	public Layer getBottomLayer() {
		return bottomLayer;
	}

	public Layer getTopLayer() {
		return topLayer;
	}

	public double[][] getUnitConnectionWeights() {
		return unitConnectionWeights;
	}

	public double[][] getWeightSteps() {
		return weightSteps;
	}

	public double[] getWeightsForBottomUnit(int unitIndex) {
		return unitConnectionWeights[unitIndex];
	}

	public double[] getWeightStepsForBottomUnit(int unitIndex) {
		return weightSteps[unitIndex];
	}

	public double[] getWeightsForTopUnit(int unitIndex) {
		double[] weights = new double[bottomLayer.size()];
		for (int i = 0; i < unitConnectionWeights.length; i++) {
			weights[i] = unitConnectionWeights[i][unitIndex];
		}
		return weights;
	}

	public double[] getWeightStepsForTopUnit(int unitIndex) {
		double[] weightStepsResult = new double[bottomLayer.size()];
		for (int i = 0; i < weightSteps.length; i++) {
			weightStepsResult[i] = weightSteps[i][unitIndex];
		}
		return weightStepsResult;
	}

	public void setWeightsForTopUnit(int unitIndex, double[] newWeights) {
		for (int i = 0; i < bottomLayer.size(); i++) {
			unitConnectionWeights[i][unitIndex] = newWeights[i];
		}
	}

	public void setWeigthStepsForTopUnit(int unitIndex, double[] newWeightSteps) {
		for (int i = 0; i < bottomLayer.size(); i++) {
			weightSteps[i][unitIndex] = newWeightSteps[i];
		}
	}

	@Override
	public String toString() {
		return topLayer.toString() + " " + bottomLayer.toString();
	}
}
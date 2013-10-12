package boltzmann.machines.restricted;

import java.util.HashMap;
import java.util.Map;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.BoltzmannMachine;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachine extends BoltzmannMachine {
	private static final long serialVersionUID = -4569400715891256872L;
	private float[][] positive;
	private float[][] negative;
	private Layer visibleLayer;
	private Layer hiddenLayer;

	public RestrictedBoltzmannMachine(Layer[] layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
		visibleLayer = layers[0];
		hiddenLayer = layers[1];
		positive = new float[visibleLayer.size()][hiddenLayer.size()];
		negative = new float[visibleLayer.size()][hiddenLayer.size()];
	}

	public void initializeVisibleLayerStates(InputStateVector initialInputStates) {
		for (int i = 0; i < initialInputStates.size(); i++) {
			Unit inputUnit = visibleLayer.getUnit(i);
			inputUnit.setState(initialInputStates.get(i));
		}
	}
	
	public void initializeHiddenLayerStates(InputStateVector initialHiddenStates) {
		for(int i = 0; i< initialHiddenStates.size(); i++) {
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			hiddenUnit.setState(initialHiddenStates.get(i));
		}
	}

	// reality phase
	public void updateHiddenUnits() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			Map<Unit, Float> unitsMap = new HashMap<>();
			for (int j = 0; j < weigths.length; j++) {
				unitsMap.put(visibleLayer.getUnit(j), weigths[j][i]);
			}
			hiddenUnit.calculateActivationEnergy(unitsMap);
			hiddenUnit.calculateStateChangeProbability();
			hiddenUnit.changeState();
		}
	}

	public void calculatePositive() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				Unit visible = visibleLayer.getUnit(i);
				positive[i][j] = visible.getState() * hidden.getActivationProbability();
			}
		}
	}

	// daydreaming phase
	public void reconstructVisibleUnits() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < visibleLayer.size(); i++) {
			Unit visibleUnit = visibleLayer.getUnit(i);
			float[] weigthsForUnit = weigths[i];
			Map<Unit, Float> unitsMap = new HashMap<>();
			for (int j = 0; j < weigthsForUnit.length; j++) {
				unitsMap.put(hiddenLayer.getUnit(j), weigthsForUnit[j]);
			}
			visibleUnit.calculateActivationEnergy(unitsMap);
			visibleUnit.calculateStateChangeProbability();
			visibleUnit.changeState();
		}
	}

	public void calculateNegative() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				Unit visible = visibleLayer.getUnit(i);
				negative[i][j] = hidden.getActivationProbability() * visible.getActivationProbability();
			}
		}
	}

	public void updateWeights(int learningSetSize) {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				weigths[i][j] += learningRate * ((positive[i][j] - negative[i][j]) / learningSetSize);
			}
		}
	}

	public float[][] getWeights() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		return weigths;
	}

	public float[] getHiddenLayerStates() {
		float[] output = new float[hiddenLayer.size()];
		for (int i = 0; i < hiddenLayer.size(); i++) {
			output[i] = hiddenLayer.getUnit(i).getState();
		}
		return output;
	}
	
	public void testVisible(InputStateVector testVector) {
		resetNetworkStates();
		initializeVisibleLayerStates(testVector);
		updateHiddenUnits();
	}
	
	public void testHidden(InputStateVector testVector) {
		resetNetworkStates();
		initializeHiddenLayerStates(testVector);
		reconstructVisibleUnits();
	}
}
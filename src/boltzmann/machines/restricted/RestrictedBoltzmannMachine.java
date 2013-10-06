package boltzmann.machines.restricted;

import java.util.HashMap;
import java.util.Map;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.machines.BoltzmannMachine;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachine extends BoltzmannMachine {
	private int[][] positive;
	private int[][] negative;
	private Layer visibleLayer;
	private Layer hiddenLayer;

	public RestrictedBoltzmannMachine(Layer[] layers) {
		super(layers);
		visibleLayer = layers[0];
		hiddenLayer = layers[1];
		positive = new int[visibleLayer.size()][hiddenLayer.size()];
		negative = new int[visibleLayer.size()][hiddenLayer.size()];
	}

	public void initializeVisibleLayerStates(InputStateVector initialInputStates) {
		for (int i = 0; i < initialInputStates.size(); i++) {
			Unit inputUnit = visibleLayer.getUnit(i);
			inputUnit.setState(initialInputStates.get(i));
		}
	}

	public void updateHiddenUnits() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			Map<Unit, Float> unitsMap = new HashMap<>();
			for(int j = 0; j<weigths.length; j++) {
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
				positive[i][j] = hidden.getState() * visible.getState();
			}
		}
	}
	
	public void calculateNegative() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				Unit visible = visibleLayer.getUnit(i);
				negative[i][j] = hidden.getState() * visible.getState();
			}
		}
	}

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

	public void updateWeights() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				weigths[i][j] += learningRate * (positive[i][j] - negative[i][j]);
			}
		}
	}
	
	public float[][] getWeights() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		return weigths;
	}
	
	public int[] getHiddenLayerStates() {
		int[] output = new int[hiddenLayer.size()];
		for(int i = 0; i<hiddenLayer.size(); i++) {
			output[i] = hiddenLayer.getUnit(i).getState();
		}
		return output;
	}
}
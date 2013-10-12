package boltzmann.machines.restricted;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.BoltzmannMachine;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachine extends BoltzmannMachine {
	private static final long serialVersionUID = -4569400715891256872L;
	private float[][] positiveGradient;
	private float[][] negativeGradient;
	private Layer visibleLayer;
	private Layer hiddenLayer;

	public RestrictedBoltzmannMachine(Layer[] layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
		visibleLayer = layers[0];
		hiddenLayer = layers[1];
		positiveGradient = new float[visibleLayer.size()][hiddenLayer.size()];
		negativeGradient = new float[visibleLayer.size()][hiddenLayer.size()];
	}

	public void initializeVisibleLayerStates(InputStateVector initialInputStates) {
		for (int i = 0; i < initialInputStates.size(); i++) {
			Unit inputUnit = visibleLayer.getUnit(i);
			inputUnit.setState(initialInputStates.get(i));
		}
	}

	public void initializeHiddenLayerStates(InputStateVector initialHiddenStates) {
		for (int i = 0; i < initialHiddenStates.size(); i++) {
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			hiddenUnit.setState(initialHiddenStates.get(i));
		}
	}

	/**
	 * reality phase
	 */
	public void updateHiddenUnits() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			float activationEnergy = 0.0f;
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			for (int j = 0; j < weigths.length; j++) {
				activationEnergy += visibleLayer.getUnit(j).getState() * weigths[j][i];
			}
			hiddenUnit.setActivationEnergy(activationEnergy);
			hiddenUnit.calculateActivationChangeProbability();
			hiddenUnit.tryToTurnOn();
		}
	}

	public void calculatePositiveGradient() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			Unit visibleUnit = visibleLayer.getUnit(i);
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hiddenUnit = hiddenLayer.getUnit(j);
				positiveGradient[i][j] = visibleUnit.getState() * hiddenUnit.getActivationProbability();
			}
		}
	}

	/**
	 * daydreaming phase
	 */
	public void reconstructVisibleUnits() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < visibleLayer.size(); i++) {
			Unit visibleUnit = visibleLayer.getUnit(i);
			float[] weigthsForUnit = weigths[i];
			float activationEnergy = 0.0f;
			for (int j = 0; j < weigthsForUnit.length; j++) {
				activationEnergy += hiddenLayer.getUnit(j).getState() * weigthsForUnit[j];
			}
			visibleUnit.setActivationEnergy(activationEnergy);
			visibleUnit.calculateActivationChangeProbability();
			visibleUnit.tryToTurnOn();
		}
	}

	public void calculateNegativeGradient() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				Unit visible = visibleLayer.getUnit(i);
				negativeGradient[i][j] = hidden.getActivationProbability() * visible.getActivationProbability();
			}
		}
	}

	public void updateWeights() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				weigths[i][j] += learningRate * (positiveGradient[i][j] - negativeGradient[i][j]);
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
	
	public void resetVisibleStates() {
		for(Unit u : visibleLayer.getUnits()) {
			u.setState(0);
		}
	}
	
	public void resetHiddenStates() {
		for(Unit u : hiddenLayer.getUnits()) {
			u.setState(0);
		}
	}

	public void testVisible(InputStateVector testVector) {
		resetUnitStates();
		initializeVisibleLayerStates(testVector);
		updateHiddenUnits();
	}

	public void testHidden(InputStateVector testVector) {
		resetUnitStates();
		initializeHiddenLayerStates(testVector);
		reconstructVisibleUnits();
	}
}

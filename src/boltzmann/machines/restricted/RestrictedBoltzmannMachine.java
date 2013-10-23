package boltzmann.machines.restricted;

import java.util.List;

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
	protected Layer visibleLayer;
	protected Layer hiddenLayer;

	public RestrictedBoltzmannMachine(List<Layer> layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
		visibleLayer = layers.get(0);
		hiddenLayer = layers.get(1);
	}
	
	public RestrictedBoltzmannMachine(Layer visibleLayer, Layer hiddenLayer, LayerConnector connector) {
		super();
		this.visibleLayer = visibleLayer;
		this.hiddenLayer = hiddenLayer;
		layers.add(visibleLayer);
		layers.add(hiddenLayer);
		connections.add(connector);
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
				Unit visibleUnit = visibleLayer.getUnit(j);
				activationEnergy += visibleUnit.getState() * weigths[j][i];
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

	public void reconstructHiddenUnits() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			float activationEnergy = 0.0f;
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			for (int j = 0; j < weigths.length; j++) {
				activationEnergy += visibleLayer.getUnit(j).getActivationProbability() * weigths[j][i];
			}
			hiddenUnit.setActivationEnergy(activationEnergy);
			hiddenUnit.calculateActivationChangeProbability();
		}
	}

	public void calculateNegativeGradient() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			Unit visible = visibleLayer.getUnit(i);
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				negativeGradient[i][j] = visible.getActivationProbability() * hidden.getActivationProbability();
			}
		}
	}

	public void updateWeights(float learningFactor) {
		LayerConnector connector = getLayerConnector(visibleLayer);
		float[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			for (int j = 0; j < weigths[i].length; j++) {
				weigths[i][j] += learningFactor * (positiveGradient[i][j] - negativeGradient[i][j]);
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

	public float[] getVisibleLayerStates() {
		float[] output = new float[visibleLayer.size()];
		for (int i = 0; i < visibleLayer.size(); i++) {
			output[i] = visibleLayer.getUnit(i).getState();
		}
		return output;
	}

	public Layer getVisibleLayer() {
		return visibleLayer;
	}

	public Layer getHiddenLayer() {
		return hiddenLayer;
	}

	public void setHiddenLayer(Layer hiddenLayer) {
		this.hiddenLayer = hiddenLayer;
	}

	public void setVisibleLayer(Layer visibleLayer) {
		this.visibleLayer = visibleLayer;
	}

	public void resetVisibleStates() {
		for (Unit u : visibleLayer.getUnits()) {
			u.setState(0);
		}
	}

	public void resetHiddenStates() {
		for (Unit u : hiddenLayer.getUnits()) {
			u.setState(0);
		}
	}

	public void clearGradients() {
		positiveGradient = new float[visibleLayer.size()][hiddenLayer.size()];
		negativeGradient = new float[visibleLayer.size()][hiddenLayer.size()];
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
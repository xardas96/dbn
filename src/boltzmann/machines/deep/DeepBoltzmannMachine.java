package boltzmann.machines.deep;

import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;

public class DeepBoltzmannMachine extends RestrictedBoltzmannMachine {
	private static final long serialVersionUID = 4192302145071680365L;

	public DeepBoltzmannMachine() {
		super();
	}
	
	public DeepBoltzmannMachine(List<Layer> layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
	}
	
	public Layer getNextLayer(Layer layer) {
		int index = layers.indexOf(layer);
		return layers.get(index + 1);
	}
	
	public Layer getPreviousLayer(Layer layer) {
		int index = layers.indexOf(layer);
		return layers.get(index - 1);
	}
	
	@Override
	public double[] getHiddenLayerStates() {
		double[] output = new double[hiddenLayer.size()];
		for (int i = 0; i < hiddenLayer.size(); i++) {
			output[i] = hiddenLayer.getUnit(i).getActivationProbability();
		}
		return output;
	}
	
	public void resetLayers() {
		visibleLayer = getFirstLayer();
		hiddenLayer = getNextLayer(visibleLayer);
	}
	
	public void ascendLayers() {
		visibleLayer = hiddenLayer;
		hiddenLayer = getNextLayer(visibleLayer);
	}
	
	public void descendLayers() {
		hiddenLayer = visibleLayer;
		visibleLayer = getPreviousLayer(hiddenLayer);
	}
	
	public Layer getLayer(int index) {
		return layers.get(index);
	}
}
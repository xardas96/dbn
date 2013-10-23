package dbn;

import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;

public class SimpleDeepBeliefNetwork extends RestrictedBoltzmannMachine {
	private static final long serialVersionUID = 4192302145071680365L;

	public SimpleDeepBeliefNetwork(List<Layer> layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
	}
	
	public Layer getNextLayer(Layer layer) {
		int index = layers.indexOf(layer);
		return layers.get(index + 1);
	}
	
	@Override
	public float[] getHiddenLayerStates() {
		float[] output = new float[hiddenLayer.size()];
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
}
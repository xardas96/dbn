package boltzmann.machines;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;

public abstract class BoltzmannMachine {
	protected Layer[] layers;
	protected List<LayerConnector> connections;
	protected float learningRate;

	public BoltzmannMachine(Layer[] layers) {
		this.layers = layers;
		connections = new ArrayList<>();
		for (int i = 0; i < layers.length - 1; i++) {
			LayerConnector connector = new LayerConnector(layers[i], layers[i + 1]);
			connections.add(connector);
		}
	}

	public LayerConnector getLayerConnector(Layer layer) {
		LayerConnector output = null;
		for (int i = 0; i < connections.size() && output == null; i++) {
			LayerConnector connector = connections.get(i);
			if (connector.getTopLayer().equals(layer) || connector.getBottomLayer().equals(layer)) {
				output = connector;
			}
		}
		return output;
	}
	
	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}
	
	public Layer[] getLayers() {
		return layers;
	}
	
	public List<LayerConnector> getConnections() {
		return connections;
	}
}
package boltzmann.machines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.units.Unit;

public abstract class BoltzmannMachine implements Serializable {
	private static final long serialVersionUID = 4802579472344660915L;
	protected Layer[] layers;
	protected List<LayerConnector> connections;
	protected float learningRate;

	public BoltzmannMachine(Layer[] layers, LayerConnectorWeightInitializer weightInitializer) {
		this.layers = layers;
		connections = new ArrayList<>();
		for (int i = 0; i < layers.length - 1; i++) {
			LayerConnector connector = new LayerConnector(layers[i], layers[i + 1], weightInitializer);
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
	
	public void resetNetworkStates() {
		for(Layer layer : layers) {
			for(Unit u : layer.getUnits()) {
				u.setState(0);
			}
		}
	}
}
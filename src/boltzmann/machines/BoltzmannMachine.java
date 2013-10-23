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
	protected List<Layer> layers;
	protected List<LayerConnector> connections;
	protected LayerConnectorWeightInitializer weightInitializer;

	public BoltzmannMachine() {
		layers = new ArrayList<>();
		connections = new ArrayList<>();
	}
	
	public BoltzmannMachine(List<Layer> layers, LayerConnectorWeightInitializer weightInitializer) {
		this.layers = layers;
		this.weightInitializer = weightInitializer;
		connections = new ArrayList<>();
		for (int i = 0; i < layers.size() - 1; i++) {
			LayerConnector connector = new LayerConnector(layers.get(i), layers.get(i + 1), weightInitializer);
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

	public List<Layer> getLayers() {
		return layers;
	}

	public Layer getFirstLayer() {
		return layers.get(0);
	}

	public Layer getLastLayer() {
		return layers.get(layers.size() - 1);
	}

	public List<LayerConnector> getConnections() {
		return connections;
	}

	public void resetUnitStates() {
		for (Layer layer : layers) {
			for (Unit u : layer.getUnits()) {
				u.setState(0);
			}
		}
	}
}
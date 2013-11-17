package dbn;

import java.io.Serializable;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.units.UnitType;

public class DeepBeliefNetwork implements Serializable {
	private static final long serialVersionUID = -8772997017509630923L;
	private DeepBoltzmannMachine dbm;

	public DeepBeliefNetwork(DeepBoltzmannMachine dbm) {
		this.dbm = dbm;
	}

	public void addClassificationLayer(LayerConnectorWeightInitializer weightInitializer, int classificationLayerSize) {
		Layer classificationLayer = new Layer(classificationLayerSize, UnitType.HIDDEN);
		LayerConnector connector = new LayerConnector(dbm.getLastLayer(), classificationLayer, weightInitializer);
		dbm.getConnections().add(connector);
		dbm.getLayers().add(classificationLayer);
	}

	public void resetLayers() {
		dbm.resetLayers();
	}

	public Layer getFirstLayer() {
		return dbm.getFirstLayer();
	}

	public Layer getLastLayer() {
		return dbm.getLastLayer();
	}

	public List<Layer> getLayers() {
		return dbm.getLayers();
	}

	public Layer getLayer(int index) {
		return dbm.getLayer(index);
	}

	public void resetStates() {
		dbm.resetStates();
	}

	public void updateUnits(Layer layer) {
		dbm.updateUnits(layer);
	}

	public Layer getNextLayer(Layer layer) {
		return dbm.getNextLayer(layer);
	}

	public LayerConnector getLayerConnector(Layer layer) {
		return dbm.getLayerConnector(layer);
	}
}
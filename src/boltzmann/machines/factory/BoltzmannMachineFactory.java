package boltzmann.machines.factory;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.deep.DeepBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.units.UnitType;
import dbn.DeepBeliefNetwork;

public abstract class BoltzmannMachineFactory {

	public static RestrictedBoltzmannMachine getRestrictedBoltzmannMachine(LayerConnectorWeightInitializer weightInitializer, int visibleLayerCapacity, int hiddenLayerCapacity) {
		Layer visibleLayer = new Layer(visibleLayerCapacity, UnitType.VISIBLE);
		Layer hiddenlayer = new Layer(hiddenLayerCapacity, UnitType.HIDDEN);
		List<Layer> layers = new ArrayList<>();
		layers.add(visibleLayer);
		layers.add(hiddenlayer);
		RestrictedBoltzmannMachine rbm = new RestrictedBoltzmannMachine(layers, weightInitializer);
		return rbm;
	}

	public static DeepBoltzmannMachine getDeepBotlzmannMachine(LayerConnectorWeightInitializer weightInitializer, Integer... layerCounts) {
		int inputLayerSize = layerCounts[0];
		List<Layer> layers = new ArrayList<>();
		layers.add(new Layer(inputLayerSize, UnitType.VISIBLE));
		for (int i = 1; i < layerCounts.length; i++) {
			layers.add(new Layer(layerCounts[i], UnitType.HIDDEN));
		}
		DeepBoltzmannMachine dbm = new DeepBoltzmannMachine(layers, weightInitializer);
		return dbm;
	}

	public static DeepBeliefNetwork getDeepBeliefNetwork(DeepBoltzmannMachine dbm, LayerConnectorWeightInitializer weightInitializer, int classificationLayerSize) {
		DeepBeliefNetwork dbn = new DeepBeliefNetwork(dbm);
		dbn.addClassificationLayer(weightInitializer, classificationLayerSize);
		return dbn;
	}
}
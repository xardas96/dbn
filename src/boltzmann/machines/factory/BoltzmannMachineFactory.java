package boltzmann.machines.factory;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.units.factory.UnitFactory.UnitType;

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
}
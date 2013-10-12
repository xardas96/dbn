package boltzmann.machines.factory;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.units.factory.UnitFactory.UnitType;

public abstract class BoltzmannMachineFactory {

	public static RestrictedBoltzmannMachine getRestrictedBoltzmannMachine(int visibleLayerCapacity, int hiddenLayerCapacity, LayerConnectorWeightInitializer weightInitializer) {
		Layer visibleLayer = new Layer(visibleLayerCapacity, UnitType.VISIBLE);
		Layer hiddenlayer = new Layer(hiddenLayerCapacity, UnitType.HIDDEN);
		RestrictedBoltzmannMachine rbm = new RestrictedBoltzmannMachine(new Layer[] { visibleLayer, hiddenlayer }, weightInitializer);
		return rbm;
	}
}
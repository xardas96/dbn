package dbn.factory;

import java.util.ArrayList;
import java.util.List;

import dbn.SimpleDeepBeliefNetwork;
import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.units.factory.UnitFactory.UnitType;

public abstract class DeepBeliefNetworkFactory {
	
	public static SimpleDeepBeliefNetwork getSimpleDeepBeliefNetwork(LayerConnectorWeightInitializer weightInitializer, Integer... layerCounts) {
		int inputLayerSize = layerCounts[0];
		List<Layer> layers = new ArrayList<>();
		layers.add(new Layer(inputLayerSize, UnitType.VISIBLE));
		for(int i = 1; i<layerCounts.length; i++) {
			layers.add(new Layer(layerCounts[i], UnitType.HIDDEN));
		}
		SimpleDeepBeliefNetwork dbn = new SimpleDeepBeliefNetwork(layers, weightInitializer);
		return dbn;
	}
}
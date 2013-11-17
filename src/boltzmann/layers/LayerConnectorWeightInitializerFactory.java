package boltzmann.layers;

import java.util.Random;

public abstract class LayerConnectorWeightInitializerFactory {

	public static LayerConnectorWeightInitializer getZeroWeightInitializer() {
		return new LayerConnectorWeightInitializer() {

			@Override
			public double getWeight() {
				return 0.0f;
			}
		};
	}

	public static LayerConnectorWeightInitializer getGaussianWeightInitializer() {
		return new LayerConnectorWeightInitializer() {
			private Random random = new Random();

			@Override
			public double getWeight() {
				return random.nextGaussian();
			}
		};
	}
}
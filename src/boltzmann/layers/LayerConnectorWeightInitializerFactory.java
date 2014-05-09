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

	public static LayerConnectorWeightInitializer getHalfAndHalfWeightInitializer() {
		return new LayerConnectorWeightInitializer() {
			private double rangeMin = -0.5;
			private double rangeMax = 0.5;
			private Random random = new Random();

			@Override
			public double getWeight() {
				return rangeMin + (rangeMax - rangeMin) * random.nextDouble();
			}
		};
	}
}
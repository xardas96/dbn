package boltzmann.machines;

import boltzmann.layers.Layer;

public abstract class BoltzmannMachine {
	protected Layer[] layers;

	public BoltzmannMachine(Layer[] layers) {
		this.layers = layers;
	}
}
package boltzmann.units;

import java.util.Random;

public class GaussianUnit extends Unit {
	private static final long serialVersionUID = -2612593259321387301L;

	public GaussianUnit(UnitType unitType) {
		super(unitType);
	}

	@Override
	public void tryToTurnOn() {
		state = activationEnergy + new Random().nextGaussian();
	}
}
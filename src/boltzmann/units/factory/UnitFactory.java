package boltzmann.units.factory;

import java.util.Random;

import boltzmann.units.Unit;
import boltzmann.units.UnitType;

public abstract class UnitFactory {

	public static Unit getUnit(UnitType unitType) {
		Unit unit = new Unit(unitType);
		if (unitType == UnitType.BIAS) {
			setupBiasUnit(unit);
		}
		return unit;
	}

	private static void setupBiasUnit(Unit unit) {
		unit.setState(1.0f);
		Random rand = new Random();
		unit.setActivationEnergy(rand.nextGaussian());
	}
}
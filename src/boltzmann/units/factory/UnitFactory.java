package boltzmann.units.factory;

import boltzmann.units.GaussianUnit;
import boltzmann.units.Unit;
import boltzmann.units.UnitType;

public abstract class UnitFactory {

	public static Unit getUnit(UnitType unitType) {
		Unit unit;
		if (unitType == UnitType.VISIBLE_GAUSSIAN) {
			unit = new GaussianUnit(unitType);
		} else {
			unit = new Unit(unitType);
			if (unitType == UnitType.BIAS) {
				setupBiasUnit(unit);
			}
		}
		return unit;
	}

	private static void setupBiasUnit(Unit unit) {
		unit.setState(1);
		unit.setActivationEnergy(0);
	}
}
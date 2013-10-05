package boltzmann.units.factory;

import boltzmann.units.BiasUnit;
import boltzmann.units.HiddenUnit;
import boltzmann.units.Unit;
import boltzmann.units.VisibleUnit;

public abstract class UnitFactory {

	public static Unit getUnit(UnitType unitType) {
		switch (unitType) {
		case VISIBLE:
			return new VisibleUnit();
		case HIDDEN:
			return new HiddenUnit();
		case BIAS:
			return new BiasUnit();
		default:
			return null;
		}
	}

	public enum UnitType {
		VISIBLE, HIDDEN, BIAS
	}
}
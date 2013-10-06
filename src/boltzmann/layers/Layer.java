package boltzmann.layers;

import boltzmann.units.Unit;
import boltzmann.units.factory.UnitFactory;
import boltzmann.units.factory.UnitFactory.UnitType;

public class Layer {
	private Unit[] units;

	public Layer(int layerCapacity, UnitType unitType) {
		units = new Unit[layerCapacity];
		for (int i = 0; i < layerCapacity; i++) {
			units[i] = UnitFactory.getUnit(unitType);
		}
	}

	public Unit getUnit(int index) {
		return units[index];
	}

	public int size() {
		return units.length;
	}
}
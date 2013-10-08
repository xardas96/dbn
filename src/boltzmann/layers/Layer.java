package boltzmann.layers;

import java.io.Serializable;

import boltzmann.units.Unit;
import boltzmann.units.factory.UnitFactory;
import boltzmann.units.factory.UnitFactory.UnitType;

public class Layer implements Serializable {
	private static final long serialVersionUID = -3697680278883271290L;
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
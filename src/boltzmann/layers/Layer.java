package boltzmann.layers;

import java.io.Serializable;

import boltzmann.units.Unit;
import boltzmann.units.UnitType;
import boltzmann.units.factory.UnitFactory;
import boltzmann.vectors.InputStateVector;

public class Layer implements Serializable {
	private static final long serialVersionUID = -3697680278883271290L;
	private Unit[] units;

	public Layer(int layerCapacity, UnitType unitType) {
		units = new Unit[layerCapacity];
		for (int i = 0; i < layerCapacity; i++) {
			units[i] = UnitFactory.getUnit(unitType);
		}
	}

	public Layer(Unit[] units) {
		this.units = units;
	}

	public void initStates(InputStateVector states) {
		for (int i = 0; i < states.size(); i++) {
			units[i].setState(states.get(i));
		}
	}

	public void reset() {
		for (Unit unit : units) {
			unit.setState(0);
		}
	}

	public double[] getStates() {
		double[] output = new double[units.length];
		for (int i = 0; i < units.length; i++) {
			output[i] = units[i].getState();
		}
		return output;
	}

	public Unit getUnit(int index) {
		return units[index];
	}

	public int size() {
		return units.length;
	}

	public Unit[] getUnits() {
		return units;
	}

	public void setUnits(Unit[] units) {
		this.units = units;
	}

	@Override
	public String toString() {
		return "Layer " + units[0].getUnitType().name() + " " + units.length;
	}
}
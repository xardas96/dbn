package boltzmann.units;

import java.util.Map;

public abstract class Unit {
	protected float activationEnergy;
	protected float activationProbability;
	protected int state;

	public float calculateActivationEnergy(Map<Unit, Float> connectedUnits) {
		activationEnergy = 0;
		for (Unit connectedUnit : connectedUnits.keySet()) {
			activationEnergy += connectedUnits.get(connectedUnit) * connectedUnit.state;
		}
		return activationEnergy;
	}

	public float calculateStateChangeProbability() {
		activationProbability = (float) (1 / (1 + Math.exp(-activationEnergy)));
		return activationProbability;
	}

	public int changeState() {
		turnOn();
		turnOff();
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	private void turnOn() {
		double rand = Math.random();
		if (activationProbability > rand) {
			state = 1;
		}
	}

	private void turnOff() {
		double rand = Math.random();
		if (1 - activationProbability > rand) {
			state = 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(activationEnergy);
		result = prime * result + Float.floatToIntBits(activationProbability);
		result = prime * result + state;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Unit)) {
			return false;
		}
		Unit other = (Unit) obj;
		if (Float.floatToIntBits(activationEnergy) != Float.floatToIntBits(other.activationEnergy)) {
			return false;
		}
		if (Float.floatToIntBits(activationProbability) != Float.floatToIntBits(other.activationProbability)) {
			return false;
		}
		if (state != other.state) {
			return false;
		}
		return true;
	}
}
package boltzmann.units;

import java.io.Serializable;

public abstract class Unit implements Serializable {
	private static final long serialVersionUID = 2284449408927311676L;
	protected float activationEnergy;
	protected float activationProbability;
	protected float state;

	public void setActivationEnergy(float activationEnergy) {
		this.activationEnergy = activationEnergy;
	}

	public void calculateActivationChangeProbability() {
		activationProbability = (float) (1.0f / (1.0f + Math.exp(-activationEnergy)));
	}

	public void setState(int state) {
		this.state = state;
	}

	public float getState() {
		return state;
	}
	
	public float getActivationProbability() {
		return activationProbability;
	}

	public void tryToTurnOn() {
		double rand = Math.random();
		if (activationProbability > rand) {
			state = 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(activationEnergy);
		result = prime * result + Float.floatToIntBits(activationProbability);
		result = prime * result + Float.floatToIntBits(state);
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
		if(!this.getClass().equals(other.getClass())) {
			return false;
		}
		return true;
	}
}
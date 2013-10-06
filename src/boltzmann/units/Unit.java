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
}
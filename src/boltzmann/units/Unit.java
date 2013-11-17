package boltzmann.units;

import java.io.Serializable;

public class Unit implements Serializable {
	private static final long serialVersionUID = 2284449408927311676L;
	private UnitType unitType;
	protected double activationEnergy;
	protected double activationProbability;
	protected double state;

	public Unit(UnitType unitType) {
		this.unitType = unitType;
	}

	public void setActivationEnergy(double activationEnergy) {
		this.activationEnergy = activationEnergy;
	}

	public double getActivationEnergy() {
		return activationEnergy;
	}

	public void calculateActivationChangeProbability() {
		activationProbability = sigmoidFunction(activationEnergy);
	}

	public double calculateActivationChangeProbabilityDerrivate() {
		return sigmoidFunction(activationEnergy) * (1.0 - sigmoidFunction(activationEnergy));
	}

	public void setState(double state) {
		this.state = state;
	}

	public double getState() {
		return state;
	}

	public double getActivationProbability() {
		return activationProbability;
	}

	public void tryToTurnOn() {
		double rand = Math.random();
		if (activationProbability > rand) {
			state = 1;
		}
	}

	private double sigmoidFunction(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}
	
	public UnitType getUnitType() {
		return unitType;
	}

	@Override
	public String toString() {
		return unitType + " " + state;
	}
}
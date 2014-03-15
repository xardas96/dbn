package boltzmann.units;

import java.io.Serializable;

import utils.Utils;

public class Unit implements Serializable {
	private static final long serialVersionUID = 2284449408927311676L;
	private UnitType unitType;
	protected double activationEnergy;
	protected double activationProbability;
	protected double state;
	protected boolean droppedOut;

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
		activationProbability = Utils.sigmoid(activationEnergy);
	}

	public double calculateActivationChangeProbabilityDerrivate() {
		return Utils.sigmoidDerivative(activationEnergy);
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
		} else {
			state = 0;
		}
	}

	public UnitType getUnitType() {
		return unitType;
	}
	
	public void setDroppedOut(boolean droppedOut) {
		this.droppedOut = droppedOut;
	}
	
	public boolean isDroppedOut() {
		return droppedOut;
	}

	@Override
	public String toString() {
		return unitType + " " + state;
	}
}
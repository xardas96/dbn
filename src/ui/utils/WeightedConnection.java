package ui.utils;

public class WeightedConnection {
	private float weight;
	private UnitVertex source;
	private UnitVertex destination;

	public WeightedConnection(float weight, UnitVertex source, UnitVertex destination) {
		this.weight = weight;
		this.source = source;
		this.destination = destination;
	}

	@Override
	public String toString() {
		return "" + weight;
	}

	public UnitVertex getDestination() {
		return destination;
	}

	public UnitVertex getSource() {
		return source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + Float.floatToIntBits(weight);
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
		if (!(obj instanceof WeightedConnection)) {
			return false;
		}
		WeightedConnection other = (WeightedConnection) obj;
		if (destination == null) {
			if (other.destination != null) {
				return false;
			}
		} else if (!destination.equals(other.destination)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (Float.floatToIntBits(weight) != Float.floatToIntBits(other.weight)) {
			return false;
		}
		return true;
	}
}
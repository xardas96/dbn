package ui.utils;

import java.awt.Point;

import boltzmann.units.Unit;

public class UnitVertex {
	private int idInLayer;
	private Unit unit;
	private Point position;

	public UnitVertex(Unit unit) {
		this.unit = unit;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public Point getPosition() {
		return position;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setIdInLayer(int idInLayer) {
		this.idInLayer = idInLayer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idInLayer;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		if (!(obj instanceof UnitVertex)) {
			return false;
		}
		UnitVertex other = (UnitVertex) obj;
		if (idInLayer != other.idInLayer) {
			return false;
		}
		if (unit == null) {
			if (other.unit != null) {
				return false;
			}
		} else if (!unit.equals(other.unit)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(idInLayer);
	}
}
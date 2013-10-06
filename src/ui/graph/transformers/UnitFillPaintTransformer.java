package ui.graph.transformers;

import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import boltzmann.units.BiasUnit;
import boltzmann.units.HiddenUnit;
import boltzmann.units.Unit;
import boltzmann.units.VisibleUnit;

public class UnitFillPaintTransformer implements Transformer<Unit, Paint> {

	@Override
	public Paint transform(Unit unit) {
		if (unit instanceof VisibleUnit && unit.getState() == 1) {
			return Color.BLUE;
		}
		if (unit instanceof HiddenUnit && unit.getState() == 1) {
			return Color.RED;
		}
		if (unit instanceof BiasUnit && unit.getState() == 1) {
			return Color.BLACK;
		}
		return Color.WHITE;
	}
}
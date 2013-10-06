package ui.graph.transformers;

import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;

import ui.utils.UnitVertex;

public class UnitPositionTransformer implements Transformer<UnitVertex, Point2D> {

	@Override
	public Point2D transform(UnitVertex unitVertex) {
		return unitVertex.getPosition();
	}
}
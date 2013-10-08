package ui.utils;

import java.awt.Point;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.machines.BoltzmannMachine;
import boltzmann.units.Unit;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public abstract class GraphBuilder {
	private static final int X_OFFSET = 100;
	private static final int Y_OFFSET = 100;

	public static Graph<UnitVertex, Float> buildGraph(BoltzmannMachine bm) {
		Graph<UnitVertex, Float> output = new UndirectedSparseGraph<>();
		for (LayerConnector connector : bm.getConnections()) {
			Layer l1 = connector.getBottomLayer();
			Layer l2 = connector.getTopLayer();
			float[][] weights = connector.getUnitConnectionWeights();
			int layerY = Y_OFFSET;
			for (int i = 0; i < weights.length; i++) {
				Unit unit1 = l1.getUnit(i);
				Point position1 = new Point((i + 1) * X_OFFSET, layerY);
				UnitVertex vertex1 = new UnitVertex(unit1);
				vertex1.setIdInLayer(i);
				vertex1.setPosition(position1);
				for (int j = 0; j < weights[i].length; j++) {
					Unit unit2 = l2.getUnit(j);
					Point position2 = new Point((j + 1) * X_OFFSET, layerY + Y_OFFSET);
					UnitVertex vertex2 = new UnitVertex(unit2);
					vertex2.setIdInLayer(j);
					vertex2.setPosition(position2);
					output.addEdge(weights[i][j], vertex2, vertex1, EdgeType.UNDIRECTED);
				}
			}
			layerY += Y_OFFSET;
		}
		return output;
	}
}
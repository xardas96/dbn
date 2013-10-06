package ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import ui.graph.mouse.Mouse;
import ui.graph.transformers.EdgeLabelClosenessTransformer;
import ui.graph.transformers.UnitDrawPaintTransformer;
import ui.graph.transformers.UnitFillPaintTransformer;
import boltzmann.units.Unit;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class GraphPanel extends JPanel {

	private static final long serialVersionUID = -8925738179912510042L;
	private Graph<Unit, Float> graph = new SparseMultigraph<>();
	private VisualizationViewer<Unit, Float> vv;
	private KKLayout<Unit, Float> layout;

	public GraphPanel() {
		setLayout(new BorderLayout(0, 0));
	}

	public void initAndShowGraph() {
		layout = new KKLayout<>(graph);
		vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);
		vv.getRenderContext().setVertexFillPaintTransformer(new UnitFillPaintTransformer());
		vv.getRenderContext().setVertexDrawPaintTransformer(new UnitDrawPaintTransformer());
		vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Float>());
		vv.getRenderContext().setEdgeLabelClosenessTransformer(new EdgeLabelClosenessTransformer(vv, 0.5f, 0.5f));
		vv.getRenderContext().setLabelOffset(15);
		Mouse mouse = new Mouse(vv.getRenderContext());
		vv.setGraphMouse(mouse);
		add(new GraphZoomScrollPane(vv), BorderLayout.CENTER);
		vv.revalidate();
		vv.repaint();
	}

	public Graph<Unit, Float> getGraph() {
		return graph;
	}

	public void setGraph(Graph<Unit, Float> graph) {
		this.graph = graph;
	}

	public VisualizationViewer<Unit, Float> getVv() {
		return vv;
	}
}
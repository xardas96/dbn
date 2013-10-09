package ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import ui.graph.mouse.Mouse;
import ui.graph.transformers.EdgeLabelClosenessTransformer;
import ui.graph.transformers.UnitDrawPaintTransformer;
import ui.graph.transformers.UnitFillPaintTransformer;
import ui.graph.transformers.UnitPositionTransformer;
import ui.utils.UnitVertex;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class GraphPanel extends JPanel {

	private static final long serialVersionUID = -8925738179912510042L;
	private Graph<UnitVertex, Float> graph;
	private VisualizationViewer<UnitVertex, Float> vv;
	private StaticLayout<UnitVertex, Float> layout;
	private GraphZoomScrollPane scrollPane;
	private Mouse mouse;

	public GraphPanel() {
		setLayout(new BorderLayout(0, 0));
	}

	public void initAndShowGraph(ModalGraphMouse.Mode mode) {
		removeAll();
		layout = new StaticLayout<>(graph, new UnitPositionTransformer());
		vv = new VisualizationViewer<>(layout);
		vv.setBackground(Color.WHITE);
		RenderContext<UnitVertex, Float> rc = vv.getRenderContext();
		rc.setVertexFillPaintTransformer(new UnitFillPaintTransformer());
		rc.setVertexDrawPaintTransformer(new UnitDrawPaintTransformer());
		rc.setVertexLabelTransformer(new ToStringLabeller<UnitVertex>());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		rc.setEdgeLabelTransformer(new ToStringLabeller<Float>());
		rc.setEdgeLabelClosenessTransformer(new EdgeLabelClosenessTransformer(vv, 0.5f, 0.5f));
		rc.setLabelOffset(15);
		mouse = new Mouse(vv.getRenderContext());
		mouse.setMode(mode);
		vv.setGraphMouse(mouse);
		scrollPane = new GraphZoomScrollPane(vv);
		add(scrollPane, BorderLayout.CENTER);
	}

	public Graph<UnitVertex, Float> getGraph() {
		return graph;
	}

	public void setGraph(Graph<UnitVertex, Float> graph) {
		this.graph = graph;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if(scrollPane != null) {
			scrollPane.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
}
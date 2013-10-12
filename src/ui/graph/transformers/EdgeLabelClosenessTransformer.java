package ui.graph.transformers;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.utils.UnitVertex;
import ui.utils.WeightedConnection;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;

public class EdgeLabelClosenessTransformer extends ConstantDirectionalEdgeValueTransformer<UnitVertex, WeightedConnection> {

    private BoundedRangeModel undirectedModel = new DefaultBoundedRangeModel(5, 0, 0, 10);
    private BoundedRangeModel directedModel = new DefaultBoundedRangeModel(7, 0, 0, 10);

    public EdgeLabelClosenessTransformer(final VisualizationViewer<UnitVertex, WeightedConnection> vv, double undirected, double directed) {
        super(undirected, directed);
        undirectedModel.setValue((int) (undirected * 10));
        directedModel.setValue((int) (directed * 10));

        undirectedModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setUndirectedValue(Double.valueOf(undirectedModel.getValue() / 10f));
                vv.repaint();
            }
        });
        directedModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setDirectedValue(Double.valueOf(directedModel.getValue() / 10f));
                vv.repaint();
            }
        });
    }
}
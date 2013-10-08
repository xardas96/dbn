package ui.graph.mouse;

import java.awt.ItemSelectable;
import java.awt.event.InputEvent;

import ui.utils.UnitVertex;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

public class Mouse extends AbstractModalGraphMouse implements ModalGraphMouse, ItemSelectable {
	protected ScalingControl scalingControl = new CrossoverScalingControl();

	public Mouse(RenderContext<UnitVertex, Float> rc) {
		super(1.1f, 1 / 1.1f);
		loadPlugins();
	}

	public Mouse(RenderContext<UnitVertex, Float> rc, float in, float out) {
		super(in, out);
		loadPlugins();
	}

	@Override
	protected final void loadPlugins() {
		translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
		scalingPlugin = new ScalingGraphMousePlugin(scalingControl, 0, out, in);

		setMode(ModalGraphMouse.Mode.TRANSFORMING);
	}

	@Override
	public void setMode(ModalGraphMouse.Mode mode) {
		if (this.mode != mode) {
			this.mode = mode;
			if (mode == ModalGraphMouse.Mode.PICKING) {
				setPickingMode();
			} else if (mode == ModalGraphMouse.Mode.TRANSFORMING) {
				setTransformingMode();
			}
		}
	}

	@Override
	protected void setPickingMode() {
		remove(translatingPlugin);
		remove(scalingPlugin);
	}

	@Override
	protected void setTransformingMode() {
		add(scalingPlugin);
		add(translatingPlugin);
	}
}
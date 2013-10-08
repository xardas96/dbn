package boltzmann.layers;

import java.io.Serializable;

public class LayerConnector implements Serializable {
	private static final long serialVersionUID = -117908422331865881L;
	private Layer bottomLayer;
	private Layer topLayer;
	private float[][] unitConnectionWeights;

	public LayerConnector(Layer bottomLayer, Layer topLayer) {
		this.bottomLayer = bottomLayer;
		this.topLayer = topLayer;
		unitConnectionWeights = new float[bottomLayer.size()][topLayer.size()];
		for (int i = 0; i < bottomLayer.size(); i++) {
			for (int j = 0; j < topLayer.size(); j++) {
				unitConnectionWeights[i][j] = (float) Math.random() * 0.1f;
			}
		}
	}
	
	public Layer getBottomLayer() {
		return bottomLayer;
	}
	
	public Layer getTopLayer() {
		return topLayer;
	}
	
	public float[][] getUnitConnectionWeights() {
		return unitConnectionWeights;
	}
}
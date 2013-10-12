package boltzmann.layers;

import java.io.Serializable;

public class LayerConnector implements Serializable {
	private static final long serialVersionUID = -117908422331865881L;
	private Layer bottomLayer;
	private Layer topLayer;
	private float[][] unitConnectionWeights;

	public LayerConnector(Layer bottomLayer, Layer topLayer, LayerConnectorWeightInitializer weightInitializer) {
		this.bottomLayer = bottomLayer;
		this.topLayer = topLayer;
		unitConnectionWeights = new float[bottomLayer.size()][topLayer.size()];
		for(int i = 0; i<unitConnectionWeights.length; i++) {
			for(int j = 0; j<unitConnectionWeights[i].length; j++) {
				unitConnectionWeights[i][j] = weightInitializer.getWeight();
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
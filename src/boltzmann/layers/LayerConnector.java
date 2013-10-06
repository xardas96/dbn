package boltzmann.layers;

public class LayerConnector {
	private Layer bottomLayer;
	private Layer topLayer;
	private float[][] unitConnectionWeights;

	public LayerConnector(Layer bottomLayer, Layer topLayer) {
		this.bottomLayer = bottomLayer;
		this.topLayer = topLayer;
		unitConnectionWeights = new float[bottomLayer.size()][topLayer.size()];
		for (int i = 0; i < bottomLayer.size(); i++) {
			for (int j = 0; j < topLayer.size(); j++) {
				unitConnectionWeights[i][j] = (float) Math.random();
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
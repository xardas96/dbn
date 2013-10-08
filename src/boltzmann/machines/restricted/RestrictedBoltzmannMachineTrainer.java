package boltzmann.machines.restricted;

import boltzmann.layers.Layer;
import boltzmann.machines.BoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachineTrainer extends BoltzmannMachineTrainer<RestrictedBoltzmannMachine> {

	public RestrictedBoltzmannMachineTrainer(RestrictedBoltzmannMachine bm, int maxEpochs, float maxError) {
		super(bm, maxEpochs, maxError);
	}

	@Override
	protected void train(InputStateVector trainingVector, int trainingVectorSize) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnits();
		bm.calculatePositive();
		bm.reconstructVisibleUnits();
		bm.updateHiddenUnits();
		bm.calculateNegative();
		bm.updateWeights(trainingVectorSize);
	}

	@Override
	protected float calculateErrorDelta(InputStateVector trainingVector) {
		float error = 0;
		Layer h = bm.getLayers()[0];
		for (int k = 0; k < h.size(); k++) {
			error += Math.pow(trainingVector.get(k) - h.getUnit(k).getActivationProbability(), 2);
		}
		return error;
	}
}
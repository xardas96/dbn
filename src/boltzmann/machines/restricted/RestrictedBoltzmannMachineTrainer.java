package boltzmann.machines.restricted;

import boltzmann.layers.Layer;
import boltzmann.machines.AdaptiveLearningFactor;
import boltzmann.machines.BoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachineTrainer extends BoltzmannMachineTrainer<RestrictedBoltzmannMachine> {

	public RestrictedBoltzmannMachineTrainer(RestrictedBoltzmannMachine bm, AdaptiveLearningFactor learningFactor, int maxEpochs, float maxError) {
		super(bm, learningFactor, maxEpochs, maxError);
	}

	@Override
	protected void train(InputStateVector trainingVector, int trainingVectorSize, float learningFactor) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnits();
		bm.calculatePositiveGradient();
		bm.resetVisibleStates();
		bm.reconstructVisibleUnits();
		bm.reconstructHiddenUnits();
		bm.calculateNegativeGradient();
		bm.updateWeights(learningFactor);
	}

	@Override
	protected float calculateErrorDelta(InputStateVector trainingVector) {
		float error = 0;
		Layer visible = bm.getLayers()[0];
		for (int k = 0; k < visible.size(); k++) {
			float delta = trainingVector.get(k) - visible.getUnit(k).getActivationProbability();
			error += delta * delta;
		}
		return error;
	}
}
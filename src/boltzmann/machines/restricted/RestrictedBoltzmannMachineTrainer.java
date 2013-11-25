package boltzmann.machines.restricted;

import boltzmann.layers.Layer;
import boltzmann.machines.BoltzmannMachineTrainer;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachineTrainer extends BoltzmannMachineTrainer<RestrictedBoltzmannMachine> {

	public RestrictedBoltzmannMachineTrainer(RestrictedBoltzmannMachine bm, AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError) {
		super(bm, learningFactor, maxEpochs, maxError);
	}

	public RestrictedBoltzmannMachineTrainer(AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError) {
		super(null, learningFactor, maxEpochs, maxError);
	}

	@Override
	protected void train(InputStateVector trainingVector, int trainingVectorSize, double learningFactor) {
		trainAsync(trainingVector, trainingVectorSize, learningFactor);
//		trainSync(trainingVector, trainingVectorSize, learningFactor);
	}

	private void trainAsync(InputStateVector trainingVector, int trainingVectorSize, double learningFactor) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnits();
		bm.calculatePositiveGradient();
		bm.resetVisibleStates();
		bm.reconstructVisibleUnits();
		bm.reconstructHiddenUnits();
		bm.calculateNegativeGradient();
		bm.updateWeights(learningFactor);
		bm.updateBiasWeights(learningFactor);
	}

	@SuppressWarnings("unused")
	private void trainSync(InputStateVector trainingVector, int trainingVectorSize, double learningFactor) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnitsSync();
		bm.calculatePositiveGradientSync();
		bm.resetVisibleStates();
		bm.reconstructVisibleUnitsSync();
		bm.reconstructHiddenUnitsSync();
		bm.calculateNegativeGradientSync();
		bm.updateWeightsSync(learningFactor);
		bm.updateBiasWeightsSync(learningFactor);
	}

	@Override
	protected double calculateErrorDelta(InputStateVector trainingVector) {
		double error = 0;
		Layer visible = bm.getVisibleLayer();
		for (int k = 0; k < visible.size(); k++) {
			double delta = trainingVector.get(k) - visible.getUnit(k).getActivationProbability();
			error += delta * delta;
		}
		return error;
	}
}
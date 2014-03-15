package boltzmann.machines.restricted;

import boltzmann.layers.Layer;
import boltzmann.machines.BoltzmannMachineTrainer;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachineTrainer extends BoltzmannMachineTrainer<RestrictedBoltzmannMachine> {

	public RestrictedBoltzmannMachineTrainer(RestrictedBoltzmannMachine bm, AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError, double momentum) {
		super(bm, learningFactor, maxEpochs, maxError, momentum);
	}

	public RestrictedBoltzmannMachineTrainer(AdaptiveLearningFactor learningFactor, int maxEpochs, double maxError, double momentum) {
		super(null, learningFactor, maxEpochs, maxError, momentum);
	}

	@Override
	protected void train(InputStateVector trainingVector, int trainingVectorSize, double learningFactor, double momentum) {
		trainAsync(trainingVector, trainingVectorSize, learningFactor, momentum);
//		trainSync(trainingVector, trainingVectorSize, learningFactor, momentum);
	}

	private void trainAsync(InputStateVector trainingVector, int trainingVectorSize, double learningFactor, double momentum) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnits();
		bm.calculatePositiveGradient();
		bm.resetVisibleStates();
		bm.reconstructVisibleUnits();
		bm.reconstructHiddenUnits();
		bm.calculateNegativeGradient();
		bm.updateWeights(learningFactor, momentum);
		bm.updateBiasWeights(learningFactor, momentum);
	}

	@SuppressWarnings("unused")
	private void trainSync(InputStateVector trainingVector, int trainingVectorSize, double learningFactor, double momentum) {
		bm.initializeVisibleLayerStates(trainingVector);
		bm.updateHiddenUnitsSync();
		bm.calculatePositiveGradientSync();
		bm.resetVisibleStates();
		bm.reconstructVisibleUnitsSync();
		bm.reconstructHiddenUnitsSync();
		bm.calculateNegativeGradientSync();
		bm.updateWeightsSync(learningFactor, momentum);
		bm.updateBiasWeightsSync(learningFactor, momentum);
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
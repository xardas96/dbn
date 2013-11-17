package boltzmann.machines.deep;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.training.Trainer;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.vectors.InputStateVector;

public class DeepBoltzmannMachineTrainer implements Trainer {
	private DeepBoltzmannMachine dbm;
	private RestrictedBoltzmannMachineTrainer rbmTrainer;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private double maxError;

	public DeepBoltzmannMachineTrainer(DeepBoltzmannMachine dbm, AdaptiveLearningFactor leariningFactor, int maxEpochs, double maxError) {
		this.dbm = dbm;
		this.learningFactor = leariningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
	}

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		rbmTrainer = new RestrictedBoltzmannMachineTrainer(learningFactor, maxEpochs, maxError);
		rbmTrainer.addTrainingStepCompletedListener(new TrainingStepCompletedListener() {

			@Override
			public void onTrainingStepComplete(int step, int trainingBatchSize) {
				if (step % 100 == 0) {
					System.out.println("step " + step + "/" + trainingBatchSize);
				}
			}

			@Override
			public void onTrainingBatchComplete(int currentEpoch, double currentError, double currentLearningFactor) {
				System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
			}
		});
		for (int i = 0; i < dbm.getLayers().size() - 1; i++) {
			System.out.println("Pretraining layers " + i + " and " + (i + 1));
			Layer firstLayer = dbm.getLayers().get(i);
			Layer secondLayer = dbm.getLayers().get(i + 1);
			RestrictedBoltzmannMachine rbm = new RestrictedBoltzmannMachine(firstLayer, secondLayer, dbm.getLayerConnector(secondLayer));
			Layer firstBias = dbm.getBiasForLayer(firstLayer);
			Layer secondBias = dbm.getBiasForLayer(secondLayer);
			rbm.setBiasLayersAndConectors(firstBias, secondBias, dbm.getLayerConnector(firstBias), dbm.getLayerConnector(secondBias));
			// dla wszystkich wektorów ucz¹cych przejdŸ przez sieæ, aktywuj i przepisz prawdopodobieñstwa jako stany
			List<InputStateVector> newTrainingVectors = new ArrayList<>();
			for (InputStateVector vector : trainingVectors) {
				InputStateVector tempVector = new InputStateVector();
				tempVector.setInputStates(vector.getInputStates());
				for (int j = 0; j < i; j++) {
					dbm.testVisible(tempVector);
					tempVector.setInputStates(dbm.getHiddenLayerStates());
					dbm.ascendLayers();
				}
				newTrainingVectors.add(tempVector);
				dbm.resetLayers();
			}
			rbmTrainer.setBm(rbm);
			rbmTrainer.train(newTrainingVectors);
		}
	}
}
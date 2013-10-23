package dbn;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.machines.AdaptiveLearningFactor;
import boltzmann.machines.TrainingStepCompletedListener;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;

public class SimpleDeepBeliefNetworkTrainer {
	private SimpleDeepBeliefNetwork dbn;
	private RestrictedBoltzmannMachineTrainer rbmTrainer;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private float maxError;

	public SimpleDeepBeliefNetworkTrainer(SimpleDeepBeliefNetwork dbn, AdaptiveLearningFactor leariningFactor, int maxEpochs, float maxError) {
		this.dbn = dbn;
		this.learningFactor = leariningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
	}

	public void train(List<InputStateVector> trainingVectors) {
		Layer firstLayer = dbn.getLayers().get(0);
		Layer secondLayer = dbn.getLayers().get(1);
		RestrictedBoltzmannMachine rbm = new RestrictedBoltzmannMachine(firstLayer, secondLayer, dbn.getLayerConnector(firstLayer));
		rbmTrainer = new RestrictedBoltzmannMachineTrainer(rbm, learningFactor, maxEpochs, maxError);
		rbmTrainer.addTrainingStepCompletedListener(new TrainingStepCompletedListener() {

			@Override
			public void onTrainingStepComplete(int step, int trainingBatchSize) {
				if (step % 100 == 0) {
					System.out.println("step " + step + "/" + trainingBatchSize);
				}
			}

			@Override
			public void onTrainingBatchComplete(int currentEpoch, float currentError, float currentLearningFactor) {
				System.out.println("Epoch: " + currentEpoch + ", error: " + currentError + ", learning factor: " + currentLearningFactor);
			}
		});
		rbmTrainer.train(trainingVectors);
		for (int i = 1; i < dbn.getLayers().size() - 1; i++) {
			System.out.println("Pretraining layer " + i);
			firstLayer = dbn.getLayers().get(i);
			secondLayer = dbn.getLayers().get(i + 1);
			rbm = new RestrictedBoltzmannMachine(firstLayer, secondLayer, dbn.getLayerConnector(secondLayer));
			//dla wszystkich wektorów ucz¹cych przejdŸ przez sieæ, aktywuj i przepisz prawdopodobieñstwa jako stany
			List<InputStateVector> newTrainingVectors = new ArrayList<>();
			for (InputStateVector vector : trainingVectors) {
				InputStateVector tempVector = new InputStateVector();
				tempVector.setInputStates(vector.getInputStates());
				for (int j = 0; j < i; j++) {
					dbn.testVisible(tempVector);
					tempVector.setInputStates(dbn.getHiddenLayerStates());
					dbn.ascendLayers();
				}
				newTrainingVectors.add(tempVector);
				dbn.resetLayers();
			}
			rbmTrainer.setBm(rbm);
			rbmTrainer.train(newTrainingVectors);
		}
	}
}
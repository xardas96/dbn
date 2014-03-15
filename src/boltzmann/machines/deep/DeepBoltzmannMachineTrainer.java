package boltzmann.machines.deep;

import io.ObjectIOManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.training.Trainer;
import boltzmann.training.TrainingStepCompletedListener;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.training.learningfactor.ConstantLearningFactor;
import boltzmann.vectors.InputStateVector;

public class DeepBoltzmannMachineTrainer implements Trainer {
	private static final double LEARINING_FACTOR_FOR_BINARY = 0.001;
	private DeepBoltzmannMachine dbm;
	private RestrictedBoltzmannMachineTrainer rbmTrainer;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private double maxError;
	private String layers;
	private int start;
	private int startLayer;
	private double momentum;

	public DeepBoltzmannMachineTrainer(DeepBoltzmannMachine dbm, AdaptiveLearningFactor leariningFactor, int maxEpochs, double maxError, double momentum) {
		this.dbm = dbm;
		this.learningFactor = leariningFactor;
		this.maxEpochs = maxEpochs;
		this.maxError = maxError;
		this.start = 0;
		this.momentum = momentum;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setStartLayer(int startLayer) {
		this.startLayer = startLayer;
	}

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		rbmTrainer = new RestrictedBoltzmannMachineTrainer(learningFactor, maxEpochs, maxError, momentum);
		rbmTrainer.setStart(start);
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
				try {
					String name = layers + currentEpoch;
					ObjectIOManager.save(dbm, new File(name + ".net"));
					ObjectIOManager.save(name, new File("stats.info"));
					ObjectIOManager.save(learningFactor, new File("learning.factor"));
					ObjectIOManager.save(momentum, new File("momentum"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		for (int i = startLayer; i < dbm.getLayers().size() - 1; i++) {
			layers = i + "_" + (i + 1) + "_";
			System.out.println("Pretraining layers " + i + " and " + (i + 1));
			Layer firstLayer = dbm.getLayers().get(i);
			Layer secondLayer = dbm.getLayers().get(i + 1);
			RestrictedBoltzmannMachine rbm = new RestrictedBoltzmannMachine(firstLayer, secondLayer, dbm.getLayerConnector(secondLayer));
			Layer firstBias = dbm.getBiasForLayer(firstLayer);
			Layer secondBias = dbm.getBiasForLayer(secondLayer);
			rbm.setBiasLayers(firstBias, secondBias);
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
			rbmTrainer.setStart(0);
			rbmTrainer.setLearningFactor(new ConstantLearningFactor(LEARINING_FACTOR_FOR_BINARY));
		}
	}
}
package dbn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.training.Trainer;
import boltzmann.training.TrainingBatchCompletedListener;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class BackpropagationDeepBeliefNetworkTrainer implements Trainer {
	private DeepBeliefNetwork dbn;
	private AdaptiveLearningFactor learningFactor;
	private List<TrainingBatchCompletedListener> trainingBatchCompletedListeners;
	private int maxEpochs;
	private double previousError;

	public BackpropagationDeepBeliefNetworkTrainer(DeepBeliefNetwork dbn, AdaptiveLearningFactor learningFactor, int maxEpochs) {
		this.dbn = dbn;
		this.dbn.resetLayers();
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
	}

	public void addTrainingBatchCompletedListener(TrainingBatchCompletedListener trainingBatchCompletedListener) {
		trainingBatchCompletedListeners.add(trainingBatchCompletedListener);
	}

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		previousError = 0;
		Layer inputLayer = dbn.getFirstLayer();
		List<Layer> outputLayers = new ArrayList<>();
		for (int i = 1; i < dbn.getLayers().size(); i++) {
			outputLayers.add(dbn.getLayer(i));
		}
		for (int i = 0; i < maxEpochs; i++) {
			Collections.shuffle(trainingVectors);
			for (InputStateVector vector : trainingVectors) {
				dbn.resetStates();
				double[] output = vector.getOutputStates();
				inputLayer.initStates(vector);
				for (int j = 0; j < outputLayers.size(); j++) {
					Layer outputLayer = outputLayers.get(j);
					dbn.updateUnits(outputLayer);
				}
				double[][] deltas = new double[outputLayers.size()][];
				Layer lastOutputLayer = dbn.getLastLayer();
				double[] lastOutputLayerDeltas = new double[lastOutputLayer.size()];
				for (int j = 0; j < lastOutputLayer.size(); j++) {
					Unit unit = lastOutputLayer.getUnit(j);
					double derrivate = unit.calculateActivationChangeProbabilityDerrivate();
					double diff = output[j] - unit.getActivationProbability();
					lastOutputLayerDeltas[j] = derrivate * diff;
				}
				deltas[deltas.length - 1] = lastOutputLayerDeltas;
				for (int j = outputLayers.size() - 2; j >= 0; j--) {
					Layer outputLayer = outputLayers.get(j);
					double[] layerDeltas = new double[outputLayer.size()];
					Layer upperLayer = dbn.getNextLayer(outputLayer);
					for (int k = 0; k < outputLayer.size(); k++) {
						Unit unit = outputLayer.getUnit(k);
						layerDeltas[k] = unit.calculateActivationChangeProbabilityDerrivate();
						LayerConnector connector = dbn.getLayerConnector(upperLayer);
						double[] weights = connector.getWeightsForBottomUnit(k);
						double sum = 0;
						for (int l = 0; l < weights.length; l++) {
							sum += deltas[j + 1][l] * weights[l];
						}
						layerDeltas[k] *= sum;
					}
					deltas[j] = layerDeltas;
				}
				for (int j = 0; j < outputLayers.size(); j++) {
					Layer outputLayer = outputLayers.get(j);
					LayerConnector connector = dbn.getLayerConnector(outputLayer);
					for (int k = 0; k < outputLayer.size(); k++) {
						double[] weights = connector.getWeightsForTopUnit(k);
						for (int l = 0; l < weights.length; l++) {
							weights[l] += learningFactor.getLearningFactor() * deltas[j][k] * connector.getBottomLayer().getUnit(l).getActivationProbability();
						}
						connector.setWeightsForTopUnit(k, weights);
					}
				}
				double currentError = 0;
				for (int j = 0; j < lastOutputLayer.size(); j++) {
					double diff = output[j] - lastOutputLayer.getUnit(j).getActivationProbability();
					currentError += diff * diff;
				}
				currentError /= 2;
				learningFactor.updateLearningFactor(previousError, currentError);
				previousError = currentError;
				for (TrainingBatchCompletedListener trainingBatchCompletedListener : trainingBatchCompletedListeners) {
					trainingBatchCompletedListener.onTrainingBatchComplete(i, previousError, learningFactor.getLearningFactor());
				}
			}
		}
	}
}
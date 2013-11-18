package dbn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.training.AdaptiveLearningFactor;
import boltzmann.training.Trainer;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class BackpropagationDeepBeliefNetworkTrainer implements Trainer {
	private DeepBeliefNetwork dbn;
	private AdaptiveLearningFactor learningFactor;
	private int maxEpochs;
	private double previousError;

	public BackpropagationDeepBeliefNetworkTrainer(DeepBeliefNetwork dbn, AdaptiveLearningFactor learningFactor, int maxEpochs) {
		this.dbn = dbn;
		this.dbn.resetLayers();
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
	}

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		previousError = 0;
		Layer inputLayer = dbn.getFirstLayer();
		List<Layer> outputLayers = new ArrayList<>();
		for (int i = 1; i < dbn.getLayers().size(); i++) {
			outputLayers.add(dbn.getLayer(i));
		}
		for (int i = 0; i < maxEpochs /*&& error > maxError*/; i++) {
			Collections.shuffle(trainingVectors);
			double currentError = 0;
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
					double der = unit.calculateActivationChangeProbabilityDerrivate();
					double delt = output[j] - unit.getActivationProbability();
					lastOutputLayerDeltas[j] = der * delt;
				}
				deltas[deltas.length - 1] = lastOutputLayerDeltas;
				for (int j = outputLayers.size() - 2; j >= 0; j--) {
					Layer outputLayer = outputLayers.get(j);
					double[] layerDeltas = new double[outputLayer.size()];
					Layer upperLayer = dbn.getNextLayer(outputLayer);
					for (int k = 0; k < outputLayer.size(); k++) {
						Unit unit = outputLayer.getUnit(k);
						layerDeltas[k] = unit.calculateActivationChangeProbabilityDerrivate();
						// TODO
						LayerConnector connector = dbn.getLayerConnector(upperLayer);
						double[] weights = connector.getWeightsForBottomUnit(k);
						// TODO
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
						// TODO
						double[] weights = connector.getWeightsForTopUnit(k);
						for (int l = 0; l < weights.length; l++) {
							weights[l] += learningFactor.getLearningFactor() * deltas[j][k] * connector.getBottomLayer().getUnit(l).getActivationProbability();
						}
						// TODO
					}
				}
				for (int j = 0; j < lastOutputLayer.size(); j++) {
					double delt = output[j] - lastOutputLayer.getUnit(j).getActivationProbability();
					double delta = delt * delt;
					currentError += delta;
				}
				System.out.println(currentError + " " + trainingVectors.indexOf(vector));
			}
			currentError /= dbn.getLastLayer().size() * trainingVectors.size();
			learningFactor.updateLearningFactor(previousError, currentError);
			previousError = currentError;
			System.out.println("Ep: " + i + "jerror: " + currentError + " learning factor: " + learningFactor.getLearningFactor());
		}
	}

	// public void train(List<InputStateVector> trainingVectors) {
	// error = double.MAX_VALUE;
	// previousError = 0;
	// for (int i = 0; i < maxEpochs && error > maxError; i++) {
	// Collections.shuffle(trainingVectors);
	// double currentError = 0;
	// double[][] layerOutputs = new double[dbn.getLayers().size()][];
	// for(InputStateVector vector : trainingVectors) {
	// dbn.resetStates();
	// double[] input = vector.getInputStates();
	// double[] output = vector.getOutputStates();
	// layerOutputs[0] = input;
	// Layer outputLayer = null;
	// for(int j = 1; j<dbn.getLayers().size(); j++) {
	// outputLayer = dbn.getLayers().get(j);
	// dbn.updateUnits(outputLayer);
	// layerOutputs[j] = outputLayer.getStates();
	// }
	// double[][] deltas = new double[dbn.getLayers().size() - 1][];
	// Layer last = dbn.getLastLayer();
	// double[] lastLayerError = new double[last.size()];
	// for(int j = 0; j<last.size(); j++) {
	// Unit unit = last.getUnit(j);
	// lastLayerError[j] = (output[j] - unit.getActivationProbability()) *
	// unit.calculateActivationChangeProbabilityDerrivate();
	// }
	// deltas[dbn.getLayers().size() - 2] = lastLayerError;
	// for(int j = dbn.getLayers().size() - 3; j>=0; j--) {
	// Unit[] units = dbn.getLayers().get(j+1).getUnits();
	// LayerConnector higherConnector =
	// dbn.getLayerConnector(dbn.getLayers().get(j+2));
	// double[][] weights = higherConnector.getUnitConnectionWeights();
	// double[] higherDeltas = deltas[j + 1];
	// double[] currentDeltas = new double[units.length];
	// for(int k = 0; k<units.length; k++) {
	// double sum = 0;
	// for(int l = 0; l<higherDeltas.length; l++) {
	// sum += higherDeltas[l] * weights[k][l];
	// }
	// currentDeltas[k] =
	// units[k].calculateActivationChangeProbabilityDerrivate()*sum;
	// }
	// deltas[j] = currentDeltas;
	// }
	// for(int j = 1; j<dbn.getLayers().size(); j++) {
	// Layer layer = dbn.getLayers().get(j);
	// LayerConnector connector = dbn.getLayerConnector(layer);
	// for(int k = 0; k<connector.getUnitConnectionWeights().length; k++) {
	// double[] weights = connector.getUnitConnectionWeights()[k];
	// for(int l = 0; l<weights.length; l++) {
	// double factor = learningFactor.getLearningFactor()*deltas[j
	// -1][l]*layerOutputs[j - 1][k];
	// weights[l] += factor;
	// }
	// }
	// }
	// for(int j = 0; j<layerOutputs[layerOutputs.length-1].length; j++) {
	// double delta =
	// Math.pow((output[j]-layerOutputs[layerOutputs.length-1][j]), 2);
	// currentError += delta;
	// }
	// double[][] outputs = new double[dbn.getLayers().size() - 1][];
	// outputs[0] = vector.getInputStates();
	// dbn.getFirstLayer().initStates(vector);
	// for(int j = 1; j<dbn.getLayers().size() - 1; j++) {
	// Layer layer = dbn.getLayers().get(j);
	// dbn.updateUnits(layer);
	// outputs[j] = layer.getStates();
	// }
	// double[][] errors = new double[dbn.getLayers().size() - 1][];
	// double[] desiredOutput = vector.getOutputStates();
	// Layer last = dbn.getLastLayer();
	// double[] lastLayerError = new double[last.size()];
	// for(int j = 0; j<last.size(); j++) {
	// Unit unit = last.getUnit(j);
	// lastLayerError[j] = (desiredOutput[j] - unit.getActivationProbability())
	// * unit.calculateActivationChangeProbabilityDerrivate();
	// }
	// errors[errors.length - 1] = lastLayerError;
	// for(int j = errors.length - 2; j>0; j--) {
	// Layer layer = dbn.getLayers().get(j);
	// double[] currentErrors = new double[layer.size()];
	// double[] previousErrors = errors[j + 1];
	// LayerConnector connector = dbn.getLayerConnector(layer);
	// double[][] weights = connector.getUnitConnectionWeights();
	// for(int k = 0; k<layer.size(); k++) {
	// Unit unit = layer.getUnit(k);
	// for(int l = 0; l<previousErrors.length; l++) {
	// currentErrors[k] += previousErrors[l] + weights[k][l];
	// }
	// currentErrors[k] *= unit.calculateActivationChangeProbabilityDerrivate();
	// }
	// errors[j] = currentErrors;
	// }
	// for(int j = 0; j<dbn.getLayers().size() -1; j++) {
	// Layer layer = dbn.getLayers().get(j);
	// LayerConnector connector = dbn.getLayerConnector(layer);
	// double[][] weights = connector.getUnitConnectionWeights();
	// for(int k = 0; k<layer.size(); k++) {
	// double[] unitWeights = weights[k];
	// for(int l = 0; l<unitWeights.length; l++) {
	// unitWeights[l] += learningFactor.getLearningFactor() * errors[j][k] *
	// outputs[j][l];
	// }
	// }
	// }
	// for(int j = 0; j<outputs[outputs.length-1].length; j++) {
	// double delta = Math.pow((desiredOutput[j]-outputs[outputs.length-1][j]),
	// 2);
	// currentError += delta;
	// }
	// }
	// currentError /= dbn.getLastLayer().size();
	// error = currentError;
	// learningFactor.updateLearningFactor(previousError, currentError);
	// previousError = error;
	// System.out.println("Error: " + currentError + " lf: "
	// +learningFactor.getLearningFactor());
	// }
	// }
}
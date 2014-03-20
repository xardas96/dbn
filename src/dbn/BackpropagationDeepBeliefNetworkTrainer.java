package dbn;

import io.ObjectIOManager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.training.Trainer;
import boltzmann.training.TrainingBatchCompletedListener;
import boltzmann.training.learningfactor.AdaptiveLearningFactor;
import boltzmann.units.Unit;
import boltzmann.vectors.InputStateVector;

public class BackpropagationDeepBeliefNetworkTrainer implements Trainer {
	private DeepBeliefNetwork dbn;
	private AdaptiveLearningFactor learningFactor;
	private List<TrainingBatchCompletedListener> trainingBatchCompletedListeners;
	private int maxEpochs;
	private double previousError;
	private double momentum;
	private TrainingThreadManager threadManager;
	private List<Layer> outputLayers = new ArrayList<>();
	private double[][] deltas;
	private int start;

	public BackpropagationDeepBeliefNetworkTrainer(DeepBeliefNetwork dbn, AdaptiveLearningFactor learningFactor, int maxEpochs, double momentum) {
		this.dbn = dbn;
		this.dbn.resetLayers();
		this.learningFactor = learningFactor;
		this.maxEpochs = maxEpochs;
		this.momentum = momentum;
		trainingBatchCompletedListeners = new ArrayList<>();
	}

	public void addTrainingBatchCompletedListener(TrainingBatchCompletedListener trainingBatchCompletedListener) {
		trainingBatchCompletedListeners.add(trainingBatchCompletedListener);
	}

	public void setStart(int start) {
		this.start = start;
	}

	@Override
	public void train(List<InputStateVector> trainingVectors) {
		previousError = 0;
		Layer inputLayer = dbn.getFirstLayer();
		outputLayers = new ArrayList<>();
		for (int i = 1; i < dbn.getLayers().size(); i++) {
			outputLayers.add(dbn.getLayer(i));
		}
		threadManager = new TrainingThreadManager(outputLayers.get(0).size());
		for (int i = start; i < maxEpochs; i++) {
			Collections.shuffle(trainingVectors);
			for (InputStateVector vector : trainingVectors) {
				dbn.resetStates();
				double[] output = vector.getOutputStates();
				inputLayer.initStates(vector);
				for (int j = 0; j < outputLayers.size(); j++) {
					Layer outputLayer = outputLayers.get(j);
					dbn.updateUnits(outputLayer);
				}
				deltas = new double[outputLayers.size()][];
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
					calculateDeltas(j);
					// calculateDeltasSync(j);
				}
				for (int j = 0; j < outputLayers.size(); j++) {
					updateWeights(j);
					// updateWeightsSync(j);
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
				System.out.println(trainingVectors.indexOf(vector));
			}
			try {
				String name = "backprop_" + i;
				ObjectIOManager.save(dbn, new File(name + ".dbn"));
				ObjectIOManager.save(name, new File("stats.info"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void calculateDeltas(int j) {
		threadManager.calculateDeltas(j);
	}

	public void calculateDeltasSync(int j) {
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

	public void updateWeights(int j) {
		threadManager.updateWeights(j);
	}

	public void updateWeightsSync(int j) {
		Layer outputLayer = outputLayers.get(j);
		LayerConnector connector = dbn.getLayerConnector(outputLayer);
		for (int k = 0; k < outputLayer.size(); k++) {
			double[] weights = connector.getWeightsForTopUnit(k);
			double[] weightSteps = connector.getWeightStepsForTopUnit(k);
			for (int l = 0; l < weights.length; l++) {
				double thisWeightStep = learningFactor.getLearningFactor() * deltas[j][k] * connector.getBottomLayer().getUnit(l).getActivationProbability();
				double momentumFactor = momentum * (thisWeightStep - weightSteps[l]);
				double deltaWeights = thisWeightStep + momentumFactor;
				weights[l] += deltaWeights;
				weightSteps[l] = deltaWeights;
			}
			connector.setWeightsForTopUnit(k, weights);
			connector.setWeigthStepsForTopUnit(k, weightSteps);
		}
	}

	private class TrainingThreadManager implements Serializable {
		private static final long serialVersionUID = -289822615383262058L;
		private int cores;
		private List<ThreadInterval> layerSplits;

		public TrainingThreadManager(int outputLayerSize) {
			cores = Runtime.getRuntime().availableProcessors();
			layerSplits = new ArrayList<>();
			int layerSplitSize = outputLayerSize / cores;
			System.out.println(cores + " " + layerSplitSize);
			for (int i = 0; i < cores; i++) {
				int layerSplit = i * layerSplitSize;
				layerSplits.add(new ThreadInterval(layerSplit, layerSplit + layerSplitSize));
			}
			int lastLayerSplit = layerSplitSize * cores;
			if (lastLayerSplit < outputLayerSize) {
				layerSplits.add(new ThreadInterval(lastLayerSplit - 1, outputLayerSize - 1));
			}
		}

		public void calculateDeltas(final int j) {
			List<Callable<Void>> tasks = new ArrayList<>();
			final Layer outputLayer = outputLayers.get(j);
			final double[] layerDeltas = new double[outputLayer.size()];
			final Layer upperLayer = dbn.getNextLayer(outputLayer);
			for (final ThreadInterval interval : layerSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit unit = outputLayer.getUnit(i);
							layerDeltas[i] = unit.calculateActivationChangeProbabilityDerrivate();
							LayerConnector connector = dbn.getLayerConnector(upperLayer);
							double[] weights = connector.getWeightsForBottomUnit(i);
							double sum = 0;
							for (int l = 0; l < weights.length; l++) {
								sum += deltas[j + 1][l] * weights[l];
							}
							layerDeltas[i] *= sum;
						}
						return null;
					}
				});
			}
			try {
				ExecutorService ex = (ExecutorService) Executors.newFixedThreadPool(cores);
				ex.invokeAll(tasks);
				ex.shutdown();
				deltas[j] = layerDeltas;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void updateWeights(final int j) {
			List<Callable<Void>> tasks = new ArrayList<>();
			Layer outputLayer = outputLayers.get(j);
			final LayerConnector connector = dbn.getLayerConnector(outputLayer);
			for (final ThreadInterval interval : layerSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							double[] weights = connector.getWeightsForTopUnit(i);
							double[] weightSteps = connector.getWeightStepsForTopUnit(i);
							for (int l = 0; l < weights.length; l++) {
								double thisWeightStep = learningFactor.getLearningFactor() * deltas[j][i] * connector.getBottomLayer().getUnit(l).getActivationProbability();
								double momentumFactor = momentum * (thisWeightStep - weightSteps[l]);
								double deltaWeights = thisWeightStep + momentumFactor;
								weights[l] += deltaWeights;
								weightSteps[l] = deltaWeights;
							}
							connector.setWeightsForTopUnit(i, weights);
							connector.setWeigthStepsForTopUnit(i, weightSteps);
						}
						return null;
					}
				});
			}
			try {
				ExecutorService ex = (ExecutorService) Executors.newFixedThreadPool(cores);
				ex.invokeAll(tasks);
				ex.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private class ThreadInterval implements Serializable {
			private static final long serialVersionUID = -2893486993086729245L;
			private int start;
			private int stop;

			public ThreadInterval(int start, int stop) {
				this.start = start;
				this.stop = stop;
			}

			public int getStart() {
				return start;
			}

			public int getStop() {
				return stop;
			}
		}
	}
}
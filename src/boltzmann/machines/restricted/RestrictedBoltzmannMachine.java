package boltzmann.machines.restricted;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.BoltzmannMachine;
import boltzmann.units.Unit;
import boltzmann.units.UnitType;
import boltzmann.vectors.InputStateVector;

public class RestrictedBoltzmannMachine extends BoltzmannMachine {
	private static final long serialVersionUID = -4569400715891256872L;
	private double[][] positiveGradient;
	private double[][] negativeGradient;
	private double[] hiddenActivationProbabilities;
	private double[] visibleStates;
	protected Layer visibleLayer;
	protected Layer hiddenLayer;
	private List<Layer> biases;
	private TrainingThreadManager threadManager;

	public RestrictedBoltzmannMachine() {
		super();
	}

	public RestrictedBoltzmannMachine(List<Layer> layers, LayerConnectorWeightInitializer weightInitializer) {
		super(layers, weightInitializer);
		visibleLayer = layers.get(0);
		hiddenLayer = layers.get(1);
		createArrays();
		createBiasLayers(weightInitializer);
		threadManager = new TrainingThreadManager();
		createThreadManager();
	}

	public RestrictedBoltzmannMachine(Layer visibleLayer, Layer hiddenLayer, LayerConnector connector) {
		super();
		this.visibleLayer = visibleLayer;
		this.hiddenLayer = hiddenLayer;
		layers.add(visibleLayer);
		layers.add(hiddenLayer);
		connections.add(connector);
		createArrays();
		createThreadManager();
	}

	public void setBiasLayers(Layer visibleBias, Layer hiddenBias) {
		biases.add(visibleBias);
		biases.add(hiddenBias);
	}

	public Layer getBiasForLayer(Layer layer) {
		return biases.get(layers.indexOf(layer));
	}

	public void createThreadManager() {
		threadManager = new TrainingThreadManager();
	}

	private void createArrays() {
		positiveGradient = new double[visibleLayer.size()][hiddenLayer.size()];
		negativeGradient = new double[visibleLayer.size()][hiddenLayer.size()];
		hiddenActivationProbabilities = new double[hiddenLayer.size()];
		biases = new ArrayList<>();
	}

	private void createBiasLayers(LayerConnectorWeightInitializer weightInitializer) {
		for (Layer layer : layers) {
			Layer biasLayer = new Layer(layer.size(), UnitType.BIAS);
			for (Unit bias : biasLayer.getUnits()) {
				bias.setActivationEnergy(weightInitializer.getWeight());
			}
			biases.add(biasLayer);
		}
	}

	public void initializeVisibleLayerStates(InputStateVector initialInputStates) {
		visibleLayer.initStates(initialInputStates);
		visibleStates = initialInputStates.getInputStates();
	}

	public void initializeHiddenLayerStates(InputStateVector initialHiddenStates) {
		hiddenLayer.initStates(initialHiddenStates);
	}

	public void updateUnits(Layer firstLayer) {
		LayerConnector connector = getLayerConnector(firstLayer);
		Layer secondLayer = connector.getBottomLayer().equals(firstLayer) ? connector.getTopLayer() : connector.getBottomLayer();
		double[][] weights = connector.getUnitConnectionWeights();
		for (int i = 0; i < firstLayer.size(); i++) {
			double activationEnergy = 0.0f;
			Unit firstUnit = firstLayer.getUnit(i);
			for (int j = 0; j < weights.length; j++) {
				Unit secondUnit = secondLayer.getUnit(j);
				activationEnergy += secondUnit.getState() * weights[j][i];
			}
			firstUnit.setActivationEnergy(activationEnergy);
			firstUnit.calculateActivationChangeProbability();
			firstUnit.tryToTurnOn();
		}
	}

	/**
	 * reality phase
	 */
	public void updateHiddenUnits() {
		threadManager.updateHiddenUnits();
	}

	public void updateHiddenUnitsSync() {
		Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
		LayerConnector connector = getLayerConnector(hiddenLayer);
		double[][] weights = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			double activationEnergy = 0.0f;
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			for (int j = 0; j < weights.length; j++) {
				Unit visibleUnit = visibleLayer.getUnit(j);
				activationEnergy += visibleUnit.getState() * weights[j][i];
			}
			activationEnergy += hiddenBias.getUnit(i).getActivationEnergy();
			hiddenUnit.setActivationEnergy(activationEnergy);
			hiddenUnit.calculateActivationChangeProbability();
			hiddenActivationProbabilities[i] = hiddenUnit.getActivationProbability();
			hiddenUnit.tryToTurnOn();
		}
	}

	public void calculatePositiveGradient() {
		threadManager.calculatePositiveGradient();
	}

	public void calculatePositiveGradientSync() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		double[][] weights = connector.getUnitConnectionWeights();
		for (int i = 0; i < weights.length; i++) {
			Unit visibleUnit = visibleLayer.getUnit(i);
			for (int j = 0; j < weights[i].length; j++) {
				Unit hiddenUnit = hiddenLayer.getUnit(j);
				positiveGradient[i][j] = visibleUnit.getState() * hiddenUnit.getActivationProbability();
			}
		}
	}

	/**
	 * daydreaming phase
	 */
	public void reconstructVisibleUnits() {
		threadManager.reconstructVisibleUnits();
	}

	public void reconstructVisibleUnitsSync() {
		Layer visibleBias = biases.get(layers.indexOf(visibleLayer));
		LayerConnector connector = getLayerConnector(hiddenLayer);
		double[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < visibleLayer.size(); i++) {
			Unit visibleUnit = visibleLayer.getUnit(i);
			double[] weightsForUnit = weigths[i];
			double activationEnergy = 0.0f;
			for (int j = 0; j < weightsForUnit.length; j++) {
				activationEnergy += hiddenLayer.getUnit(j).getActivationProbability() * weightsForUnit[j];
			}
			activationEnergy += visibleBias.getUnit(i).getActivationEnergy();
			visibleUnit.setActivationEnergy(activationEnergy);
			visibleUnit.calculateActivationChangeProbability();
			visibleUnit.tryToTurnOn();
		}
	}

	public void reconstructHiddenUnits() {
		threadManager.reconstructHiddenUnits();
	}

	public void reconstructHiddenUnitsSync() {
		Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
		LayerConnector connector = getLayerConnector(hiddenLayer);
		double[][] weights = connector.getUnitConnectionWeights();
		for (int i = 0; i < hiddenLayer.size(); i++) {
			double activationEnergy = 0.0f;
			Unit hiddenUnit = hiddenLayer.getUnit(i);
			for (int j = 0; j < weights.length; j++) {
				activationEnergy += visibleLayer.getUnit(j).getActivationProbability() * weights[j][i];
			}
			activationEnergy += hiddenBias.getUnit(i).getActivationEnergy();
			hiddenUnit.setActivationEnergy(activationEnergy);
			hiddenUnit.calculateActivationChangeProbability();
		}
	}

	public void calculateNegativeGradient() {
		threadManager.calculateNegativeGradient();
	}

	public void calculateNegativeGradientSync() {
		LayerConnector connector = getLayerConnector(hiddenLayer);
		double[][] weigths = connector.getUnitConnectionWeights();
		for (int i = 0; i < weigths.length; i++) {
			Unit visible = visibleLayer.getUnit(i);
			for (int j = 0; j < weigths[i].length; j++) {
				Unit hidden = hiddenLayer.getUnit(j);
				negativeGradient[i][j] = visible.getActivationProbability() * hidden.getActivationProbability();
			}
		}
	}

	public void updateWeights(double learningFactor) {
		threadManager.updateWeights(learningFactor);
	}

	public void updateWeightsSync(double learningFactor) {
		LayerConnector connector = getLayerConnector(visibleLayer);
		double[][] weights = connector.getUnitConnectionWeights();
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				weights[i][j] += learningFactor * (positiveGradient[i][j] - negativeGradient[i][j]);
			}
		}
	}

	public void updateBiasWeights(double learningFactor) {
		threadManager.updateBiasWeights(learningFactor);
	}

	public void updateBiasWeightsSync(double learningFactor) {
		Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
		for (int i = 0; i < hiddenBias.size(); i++) {
			Unit hiddenBiasUnit = hiddenBias.getUnit(i);
			double factor = learningFactor * (hiddenActivationProbabilities[i] - hiddenLayer.getUnit(i).getActivationProbability());
			hiddenBiasUnit.setActivationEnergy(hiddenBiasUnit.getActivationEnergy() + factor);
		}
		Layer visibleBias = biases.get(layers.indexOf(visibleLayer));
		for (int i = 0; i < visibleBias.size(); i++) {
			Unit visibleBiasUnit = visibleBias.getUnit(i);
			double factor = learningFactor * (visibleStates[i] - visibleLayer.getUnit(i).getActivationProbability());
			visibleBiasUnit.setActivationEnergy(visibleBiasUnit.getActivationEnergy() + factor);
		}
	}

	public double[][] getWeights() {
		LayerConnector connector = getLayerConnector(visibleLayer);
		double[][] weights = connector.getUnitConnectionWeights();
		return weights;
	}

	public double[] getHiddenLayerStates() {
		return hiddenLayer.getStates();
	}

	public double[] getVisibleLayerStates() {
		return visibleLayer.getStates();
	}

	public Layer getVisibleLayer() {
		return visibleLayer;
	}

	public Layer getHiddenLayer() {
		return hiddenLayer;
	}

	public void setHiddenLayer(Layer hiddenLayer) {
		this.hiddenLayer = hiddenLayer;
	}

	public void setVisibleLayer(Layer visibleLayer) {
		this.visibleLayer = visibleLayer;
	}

	public void resetVisibleStates() {
		visibleLayer.reset();
	}

	public void resetHiddenStates() {
		hiddenLayer.reset();
	}

	public void testVisible(InputStateVector testVector) {
		resetStates();
		initializeVisibleLayerStates(testVector);
		updateHiddenUnits();
	}

	public void testHidden(InputStateVector testVector) {
		resetStates();
		initializeHiddenLayerStates(testVector);
		reconstructVisibleUnits();
	}

	private class TrainingThreadManager implements Serializable {
		private static final long serialVersionUID = -289822615383262058L;
		private int cores;
		private List<ThreadInterval> visibleSplits;
		private List<ThreadInterval> hiddenSplits;

		public TrainingThreadManager() {
			cores = Runtime.getRuntime().availableProcessors();
			visibleSplits = new ArrayList<>();
			hiddenSplits = new ArrayList<>();
			int visibleSplitSize = visibleLayer.size() / cores;
			int hiddenSplitSize = hiddenLayer.size() / cores;
			System.out.println(cores + " " + visibleSplitSize + " " + hiddenSplitSize);
			for (int i = 0; i < cores; i++) {
				int visibleSplit = i * visibleSplitSize;
				int hiddenSplit = i * hiddenSplitSize;
				visibleSplits.add(new ThreadInterval(visibleSplit, visibleSplit + visibleSplitSize));
				hiddenSplits.add(new ThreadInterval(hiddenSplit, hiddenSplit + hiddenSplitSize));
			}
			int lastVisibleSplit = visibleSplitSize * cores;
			int lastHiddenSplit = hiddenSplitSize * cores;
			if (lastVisibleSplit < visibleLayer.size()) {
				visibleSplits.add(new ThreadInterval(lastVisibleSplit - 1, visibleLayer.size() - 1));
			}
			if (lastHiddenSplit < hiddenLayer.size()) {
				hiddenSplits.add(new ThreadInterval(lastHiddenSplit - 1, hiddenLayer.size() - 1));
			}
		}

		public void calculateNegativeGradient() {
			LayerConnector connector = getLayerConnector(hiddenLayer);
			final double[][] weigths = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : visibleSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit visible = visibleLayer.getUnit(i);
							for (int j = 0; j < weigths[i].length; j++) {
								Unit hidden = hiddenLayer.getUnit(j);
								negativeGradient[i][j] = visible.getActivationProbability() * hidden.getActivationProbability();
							}
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

		public void calculatePositiveGradient() {
			LayerConnector connector = getLayerConnector(hiddenLayer);
			final double[][] weights = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : visibleSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit visibleUnit = visibleLayer.getUnit(i);
							for (int j = 0; j < weights[i].length; j++) {
								Unit hiddenUnit = hiddenLayer.getUnit(j);
								positiveGradient[i][j] = visibleUnit.getState() * hiddenUnit.getActivationProbability();
							}
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

		public void updateWeights(final double learningFactor) {
			LayerConnector connector = getLayerConnector(visibleLayer);
			final double[][] weights = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : visibleSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							for (int j = 0; j < weights[i].length; j++) {
								weights[i][j] += learningFactor * (positiveGradient[i][j] - negativeGradient[i][j]);
							}
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

		public void updateBiasWeights(final double learningFactor) {
			final Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
			List<Callable<Void>> hiddenTasks = new ArrayList<>();
			for (final ThreadInterval interval : hiddenSplits) {
				hiddenTasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit hiddenBiasUnit = hiddenBias.getUnit(i);
							double factor = learningFactor * (hiddenActivationProbabilities[i] - hiddenLayer.getUnit(i).getActivationProbability());
							hiddenBiasUnit.setActivationEnergy(hiddenBiasUnit.getActivationEnergy() + factor);
						}
						return null;
					}
				});
			}
			List<Callable<Void>> visibleTasks = new ArrayList<>();
			final Layer visibleBias = biases.get(layers.indexOf(visibleLayer));
			for (final ThreadInterval interval : visibleSplits) {
				visibleTasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit visibleBiasUnit = visibleBias.getUnit(i);
							double factor = learningFactor * (visibleStates[i] - visibleLayer.getUnit(i).getActivationProbability());
							visibleBiasUnit.setActivationEnergy(visibleBiasUnit.getActivationEnergy() + factor);
						}
						return null;
					}
				});
			}
			try {
				ExecutorService ex = (ExecutorService) Executors.newFixedThreadPool(cores);
				ex.invokeAll(hiddenTasks);
				ex.invokeAll(visibleTasks);
				ex.shutdown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void reconstructHiddenUnits() {
			final Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
			LayerConnector connector = getLayerConnector(hiddenLayer);
			final double[][] weights = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : hiddenSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							double activationEnergy = 0.0f;
							Unit hiddenUnit = hiddenLayer.getUnit(i);
							for (int j = 0; j < weights.length; j++) {
								activationEnergy += visibleLayer.getUnit(j).getActivationProbability() * weights[j][i];
							}
							activationEnergy += hiddenBias.getUnit(i).getActivationEnergy();
							hiddenUnit.setActivationEnergy(activationEnergy);
							hiddenUnit.calculateActivationChangeProbability();
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

		public void reconstructVisibleUnits() {
			final Layer visibleBias = biases.get(layers.indexOf(visibleLayer));
			LayerConnector connector = getLayerConnector(hiddenLayer);
			final double[][] weigths = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : visibleSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							Unit visibleUnit = visibleLayer.getUnit(i);
							double[] weightsForUnit = weigths[i];
							double activationEnergy = 0.0f;
							for (int j = 0; j < weightsForUnit.length; j++) {
								activationEnergy += hiddenLayer.getUnit(j).getActivationProbability() * weightsForUnit[j];
							}
							activationEnergy += visibleBias.getUnit(i).getActivationEnergy();
							visibleUnit.setActivationEnergy(activationEnergy);
							visibleUnit.calculateActivationChangeProbability();
							visibleUnit.tryToTurnOn();
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

		public void updateHiddenUnits() {
			final Layer hiddenBias = biases.get(layers.indexOf(hiddenLayer));
			LayerConnector connector = getLayerConnector(hiddenLayer);
			final double[][] weights = connector.getUnitConnectionWeights();
			List<Callable<Void>> tasks = new ArrayList<>();
			for (final ThreadInterval interval : hiddenSplits) {
				tasks.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (int i = interval.getStart(); i < interval.getStop(); i++) {
							double activationEnergy = 0.0f;
							Unit hiddenUnit = hiddenLayer.getUnit(i);
							for (int j = 0; j < weights.length; j++) {
								Unit visibleUnit = visibleLayer.getUnit(j);
								activationEnergy += visibleUnit.getState() * weights[j][i];
							}
							activationEnergy += hiddenBias.getUnit(i).getActivationEnergy();
							hiddenUnit.setActivationEnergy(activationEnergy);
							hiddenUnit.calculateActivationChangeProbability();
							hiddenActivationProbabilities[i] = hiddenUnit.getActivationProbability();
							hiddenUnit.tryToTurnOn();
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
	}

	private class ThreadInterval {
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
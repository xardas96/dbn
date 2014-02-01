package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import boltzmann.vectors.InputStateVector;

public class TrainingSetGenerator implements Serializable {
	private static final long serialVersionUID = -4006544877982048781L;
	private int minInstancesTreshold;
	private Map<String, List<InputStateVector>> classInstances;
	private List<InputStateVector> trainingSet;
	private List<InputStateVector> testingSet;
	private Random random;

	public TrainingSetGenerator(int minInstancesTreshold) {
		this.minInstancesTreshold = minInstancesTreshold;
		trainingSet = new ArrayList<>();
		testingSet = new ArrayList<>();
		classInstances = new HashMap<>();
		random = new Random();
	}

	public void splitData(List<InputStateVector> data) {
		trainingSet.clear();
		testingSet.clear();
		classInstances.clear();
		for (InputStateVector vector : data) {
			String label = vector.getLabel();
			List<InputStateVector> classData = classInstances.get(label);
			if (classData == null) {
				classData = new ArrayList<>();
			}
			classData.add(vector);
			classInstances.put(label, classData);
		}
		for (String label : classInstances.keySet()) {
			List<InputStateVector> instances = classInstances.get(label);
			int instancesSize = instances.size();
			if (instancesSize >= minInstancesTreshold) {
				List<InputStateVector> trainingList = instances.subList(0, minInstancesTreshold);
				List<InputStateVector> testingList = instances.subList(minInstancesTreshold + 1, instances.size());
				trainingSet.addAll(trainingList);
				testingSet.addAll(testingList);
			} else {
				List<InputStateVector> trainingList = new ArrayList<>();
				int halfSize = instancesSize / 2;
				List<InputStateVector> halfInstances = instances.subList(0, halfSize - 1);
				List<InputStateVector> testingList = instances.subList(halfSize, instances.size());
				int i = 0;
				while ((i + halfSize) <= minInstancesTreshold) {
					trainingList.addAll(halfInstances);
					i += halfSize;
				}
				if (trainingList.size() < minInstancesTreshold) {
					while (trainingList.size() != minInstancesTreshold) {
						int randInstance = random.nextInt(halfSize - 1);
						trainingList.add(halfInstances.get(randInstance));
					}
				}
				trainingSet.addAll(trainingList);
				testingSet.addAll(testingList);
			}
		}
	}

	public List<InputStateVector> getTrainingSet() {
		return trainingSet;
	}

	public List<InputStateVector> getTestingSet() {
		return testingSet;
	}
}
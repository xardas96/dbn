package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boltzmann.vectors.InputStateVector;

public abstract class ClassDistributionCounter {

	public static Map<String, Integer> generateClassDistribution(List<InputStateVector> data) {
		Map<String, Integer> counts = new HashMap<>();
		for (InputStateVector vec : data) {
			String label = vec.getLabel();
			Integer value = counts.get(label);
			int c = value == null ? 0 : value.intValue();
			c++;
			counts.put(label, c);
		}
		return counts;
	}
}
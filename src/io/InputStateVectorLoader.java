package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import boltzmann.vectors.InputStateVector;

public abstract class InputStateVectorLoader {
	
	public static List<InputStateVector> loadFromFile(File file) throws IOException {
		List<InputStateVector> output = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] inputs = line.split(",");
			int[] states = new int[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				states[i] = Integer.valueOf(inputs[i]);
			}
			output.add(new InputStateVector(states));
		}
		reader.close();
		return output;
	}
}
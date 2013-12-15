package testsets.mnist;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import boltzmann.vectors.InputStateVector;

public class MNISTReader {
	private DataInputStream labelsBuf;
	private DataInputStream imagesBuf;
	private Random random;
	private Map<String, List<MNISTDigitElement>> trainingSet;
	private Map<String, List<MNISTDigitElement>> testSet;
	private int rows;
	private int cols;
	private int count;
	private int current;

	public MNISTReader(File labelsFile, File imagesFile) {
		random = new Random();
		trainingSet = new HashMap<String, List<MNISTDigitElement>>();
		testSet = new HashMap<String, List<MNISTDigitElement>>();
		try {
			labelsBuf = new DataInputStream(new GZIPInputStream(new FileInputStream(labelsFile)));
			imagesBuf = new DataInputStream(new GZIPInputStream(new FileInputStream(imagesFile)));
		} catch (IOException e) {
		}
	}

	public boolean verify() {
		boolean valid = false;
		if (labelsBuf != null && imagesBuf != null) {
			try {
				int magic = labelsBuf.readInt();
				if (magic == 2049) {
					int labelCount = labelsBuf.readInt();
					magic = imagesBuf.readInt();
					if (magic == 2051) {
						int imageCount = imagesBuf.readInt();
						rows = imagesBuf.readInt();
						cols = imagesBuf.readInt();
						valid = labelCount == imageCount;
						if (valid = labelCount == imageCount) {
							count = imageCount;
						}
					}
				}
			} catch (IOException e) {
				valid = false;
			}
		}
		return valid;
	}

	public void createTrainingSet(int minimalSizeForEveryDigit) {
		boolean done = false;
		while (!done || !hasMoreElements()) {
			MNISTDigitElement i = nextElement();
			if (random.nextDouble() > 0.3) {
				List<MNISTDigitElement> digitList = testSet.get(i.getLabel());
				if (digitList == null) {
					digitList = new ArrayList<>();
				}
				digitList.add(i);
				testSet.put(i.getLabel(), digitList);

			} else {
				List<MNISTDigitElement> digitList = trainingSet.get(i.getLabel());
				if (digitList == null) {
					digitList = new ArrayList<>();
				}
				digitList.add(i);
				trainingSet.put(i.getLabel(), digitList);
			}
			boolean allSizesGood = true;
			for (String digit : testSet.keySet()) {
				allSizesGood &= testSet.get(digit).size() >= minimalSizeForEveryDigit - (minimalSizeForEveryDigit / 2);
			}
			for (String digit : trainingSet.keySet()) {
				allSizesGood &= trainingSet.get(digit).size() >= minimalSizeForEveryDigit;
			}
			done = allSizesGood;
		}
	}

	public List<InputStateVector> getTrainingSetItems(boolean binarize) {
		List<InputStateVector> output = new ArrayList<>();
		for (List<MNISTDigitElement> element : trainingSet.values()) {
			for (MNISTDigitElement el : element) {	
				if(binarize) {
					el.binarize();
				}
				output.add(el);
			}
		}
		return output;
	}

	public Map<String, List<MNISTDigitElement>> getTestSet() {
		return testSet;
	}

	public List<InputStateVector> getTestingSetItems(boolean binarize) {
		List<InputStateVector> output = new ArrayList<>();
		for (List<MNISTDigitElement> element : testSet.values()) {
			for (MNISTDigitElement el : element) {
				if(binarize) {
					el.binarize();
				}
				output.add(el);
			}
		}
		return output;
	}

	public int getCols() {
		return cols;
	}

	public int getRows() {
		return rows;
	}

	public MNISTDigitElement getTestItem(int label) {
		Random rand = new Random();
		List<MNISTDigitElement> list = testSet.get(String.valueOf(label));
		return list.get(rand.nextInt(list.size()));
	}

	public boolean hasMoreElements() {
		return current < count;
	}

	public MNISTDigitElement nextElement() {
		MNISTDigitElement m = null;
		try {
			int label = labelsBuf.readUnsignedByte();
			double data[] = new double[rows * cols];
			for (int i = 0; i < data.length; i++) {
				data[i] = imagesBuf.readUnsignedByte();
			}
			m = new MNISTDigitElement(data);
			m.setLabel(label);
			m.setOutputStates(generateDesiredDBNOutput(m.getLabel()));
		} catch (IOException e) {
			current = count;
		} finally {
			current++;
		}
		return m;
	}
	
	private double[] generateDesiredDBNOutput(String label) {
		double[] output = new double[10];
		output[Integer.valueOf(label)] = 1;
		return output;
	}
}
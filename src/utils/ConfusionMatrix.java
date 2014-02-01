package utils;

public class ConfusionMatrix {
	private int truePositive;
	private int trueNegative;
	private int falsePositive;
	private int falseNegative;

	public int getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(int truePositive) {
		this.truePositive = truePositive;
	}

	public int getTrueNegative() {
		return trueNegative;
	}

	public void setTrueNegative(int trueNegative) {
		this.trueNegative = trueNegative;
	}

	public int getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(int falsePositive) {
		this.falsePositive = falsePositive;
	}

	public int getFalseNegative() {
		return falseNegative;
	}

	public void setFalseNegative(int falseNegative) {
		this.falseNegative = falseNegative;
	}
	
	public void increaseTruePositive() {
		truePositive++;
	}
	
	public void increaseTrueNegative() {
		trueNegative++;
	}
	
	public void increaseFalsePositive() {
		falsePositive++;
	}
	
	public void increaseFalseNegative() {
		falseNegative++;
	}

	public double getAccuracy() {
		double nom = truePositive + trueNegative;
		double denom = nom + falsePositive + falseNegative;
		return nom / denom;
	}

	public double getPrecision() {
		double nom = truePositive;
		double denom = nom + falsePositive;
		return nom / denom;
	}

	public double getRecall() {
		double nom = truePositive;
		double denom = nom + falseNegative;
		return nom / denom;
	}

	public double getFScore() {
		double precision = getPrecision();
		double recall = getRecall();
		double nom = 2 * precision * recall;
		double denom = precision + recall;
		return nom / denom;
	}
}
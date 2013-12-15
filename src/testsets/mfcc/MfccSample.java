package testsets.mfcc;

import java.util.ArrayList;
import java.util.List;

public class MfccSample {
	private List<MfccVector> vectors;
	private String transcription;
	private List<String> possiblePhonemes;

	public MfccSample() {
		vectors = new ArrayList<>();
	}

	public void addVector(MfccVector vector) {
		vectors.add(vector);
	}

	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public String getTranscription() {
		return transcription;
	}

	public void setPossiblePhonemes(List<String> possiblePhonemes) {
		this.possiblePhonemes = possiblePhonemes;
	}

	public List<String> getPossiblePhonemes() {
		return possiblePhonemes;
	}
	
	@Override
	public String toString() {
		return transcription;
	}
}
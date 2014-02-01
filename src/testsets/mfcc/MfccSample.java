package testsets.mfcc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MfccSample implements Serializable {
	private static final long serialVersionUID = 7459153355124894344L;
	private List<PhoneTiming> vectors;
	private String transcription;

	public MfccSample() {
		vectors = new ArrayList<>();
	}
	
	public void setVectors(List<PhoneTiming> vectors) {
		this.vectors = vectors;
	}
	
	public List<PhoneTiming> getVectors() {
		return vectors;
	}
	
	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	@Override
	public String toString() {
		return transcription;
	}
}
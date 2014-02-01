package testsets.mfcc;

import java.io.Serializable;

public class PhoneTiming implements Serializable {
	private static final long serialVersionUID = 2245516871635750782L;
	private double start;
	private double stop;
	private int frames;
	private MfccFrameVector mfccs;

	public PhoneTiming() {
		mfccs = new MfccFrameVector();
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getStart() {
		return start;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getStop() {
		return stop;
	}

	public void setPhone(String phone) {
		mfccs.setLabel(phone);
	}
	
	public void setPhone(double[] phone) {
		mfccs.setOutputStates(phone);
	}

	public String getPhone() {
		return mfccs.getLabel();
	}

	public double getInterval() {
		return stop - start;
	}

	public void calculateFramesCount() {
		double interval = getInterval();
		frames = (int) Math.round(interval * MfccParams.FRAME_DURATION);
	}

	public int getFrames() {
		return frames;
	}

	public void setMfccs(Double[] array) {
		mfccs.setInputStates(array);
	}

	public MfccFrameVector getMfccs() {
		return mfccs;
	}

	@Override
	public String toString() {
		return mfccs.getLabel() + " [" + start + " ; " + stop + "]";
	}
}
package testsets.mfcc;

public abstract class MfccParams {
	/**
	 * MFCC_D_A_0 - MFCC & deltas & accelerations & C0
	 * reading only MFCCs and deltas (without C0 & acceleration)
	 */
	public static final int FRAMES = 82;
	public static final int MFCC_COUNT = 24;
	public static final int VECTOR_SIZE = FRAMES * MFCC_COUNT;
	public static final int FRAME_DURATION = 100;
}
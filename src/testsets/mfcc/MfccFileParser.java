package testsets.mfcc;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MfccFileParser {
	private static final String MFCC = ".mfc.txt";
	private static final String LABEL = ".lab";
	private static final String TIMING = ".TextGrid";
	private String directoryPath;
	private List<String> phones;
	private boolean removeSilence;

	public MfccFileParser(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public void setRemoveSilence(boolean removeSilence) {
		this.removeSilence = removeSilence;
	}
	
	public List<MfccSample> loadSamples() throws Exception {
		List<MfccSample> output = new ArrayList<>();
		File phones = new File(directoryPath + "\\phones");
		this.phones = getPhones(phones, removeSilence);
		File dir = new File(directoryPath + "\\data");
		File[] labels = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(LABEL);
			}
		});
		File[] mfccs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(MFCC);
			}
		});
		File[] timings = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(TIMING);
			}
		});
		for (int i = 0; i < labels.length; i++) {
			File label = labels[i];
			File mfcc = mfccs[i];
			File timing = timings[i];
			String lab = getLabel(label);
			List<List<Double>> mfcFrames = getMfccFrames(mfcc, 4);
			List<PhoneTiming> phonemeTimings = getPhonemeTimings(timing);
			int k = 0;
			for (int j = 0; j < phonemeTimings.size(); j++) {
				PhoneTiming pT = phonemeTimings.get(j);
				int frames = pT.getFrames();
				List<Double> mfcs = new ArrayList<>();
				for (int l = k; l < k + frames; l++) {
					mfcs.addAll(mfcFrames.get(l));
				}
				Double[] m = new Double[MfccParams.VECTOR_SIZE];
				m = mfcs.toArray(m);
				pT.setMfccs(m);
				k += frames;
			}
			System.out.println(i + "/" + labels.length);
			MfccSample sample = new MfccSample();
			sample.setTranscription(lab);
			sample.setVectors(phonemeTimings);
			output.add(sample);
		}
		if (removeSilence) {
			for (MfccSample sample : output) {
				List<PhoneTiming> pTs = sample.getVectors();
				List<PhoneTiming> silence = new ArrayList<>();
				for (PhoneTiming pt : pTs) {
					if (pt.getPhone().isEmpty() || pt.getPhone().equals("sil") || pt.getPhone().equals("sp")) {
						silence.add(pt);
					}
				}
				pTs.removeAll(silence);
				sample.setVectors(pTs);
			}
		}
		for (MfccSample sample : output) {
			List<PhoneTiming> pTs = sample.getVectors();
			for (PhoneTiming pt : pTs) {
				double[] outputStates = new double[this.phones.size()];
				outputStates[this.phones.indexOf(pt.getPhone())] = 1.0;
				pt.setPhone(outputStates);
			}
		}
		return output;
	}

	public List<String> getPhonesList() {
		return phones;
	}

	public List<String> getPhones(File phones, boolean removeSilence) throws Exception {
		List<String> phoneList = Files.readAllLines(phones.toPath(), Charset.forName("UTF-8"));
		if (removeSilence) {
			phoneList.remove("sil");
			phoneList.remove("sp");
		}
		for(int i = 0; i<phoneList.size(); i++) {
			String newPhone = changePhone(phoneList.get(i));
			phoneList.set(i, newPhone);
		}
		return phoneList;
	}
	
	private static String changePhone(String phone) {
		String newPhone = phone;
		if (phone.equals("\\305\\233")) {
			newPhone =  "œ";
		}
		else if (phone.equals("\\304\\207")) {
			newPhone =  "æ";
		}
		else if (phone.equals("\\305\\204")) {
			newPhone =  "ñ";
		}
		else if (phone.equals("d\\305\\272")) {
			newPhone =  "dŸ";
		}
		else if (phone.equals("\\305\\272")) {
			newPhone =  "Ÿ";
		}
		else if (phone.equals("\\305\\202")) {
			newPhone =  "³";
		}
		else if (phone.equals("d\\305\\274")) {
			newPhone =  "d¿";
		}
		else if (phone.equals("\\305\\274")) {
			newPhone =  "¿";
		}
		return newPhone;
	}

	private String getLabel(File label) throws Exception {
		List<String> labs = Files.readAllLines(label.toPath(), Charset.forName("UTF-8"));
		return labs.get(0);
	}

	private List<List<Double>> getMfccFrames(File mfccs, int numberOfLines) throws Exception {
		int tierOne = MfccParams.MFCC_COUNT / 2;
		int tierTwo = MfccParams.MFCC_COUNT;
		int tierGap = 1;
		List<List<Double>> output = new ArrayList<>();
		List<String> mfcc = Files.readAllLines(mfccs.toPath(), Charset.forName("UTF-8"));
		for (int i = 1; i < mfcc.size() - numberOfLines; i += numberOfLines) {
			String line = "";
			for (int j = 0; j < numberOfLines; j++) {
				line += mfcc.get(i + j);
			}
			String[] split = line.split(" ");
			List<Double> mfcs = new ArrayList<>();
			int j = 1;
			while (mfcs.size() < tierOne) {
				String val = split[j];
				if (!val.equals(" ") && !val.equals("")) {
					mfcs.add(Double.valueOf(val));
				}
				j++;
			}
			for (int k = 0; k < tierGap; k++) {
				while (split[j].equals(" ") || split[j].equals("")) {
					j++;
				}
				j++;
			}
			while (mfcs.size() < tierTwo) {
				String val = split[j];
				if (!val.equals(" ") && !val.equals("")) {
					mfcs.add(Double.valueOf(val));
				}
				j++;
			}
			output.add(mfcs);
		}
		return output;
	}

	private List<PhoneTiming> getPhonemeTimings(File timing) throws Exception {
		int startLine = 13;
		int lineOffset = 4;
		List<PhoneTiming> output = new ArrayList<>();
		List<String> times = Files.readAllLines(timing.toPath(), Charset.forName("UTF-8"));
		String timingCount = times.get(startLine);
		int count = Integer.valueOf(timingCount.split(" = ")[1]);
		for (int i = startLine + 1; i < count * lineOffset + startLine + 1; i += lineOffset) {
			String start = times.get(i + 1);
			String stop = times.get(i + 2);
			String text = times.get(i + 3);
			PhoneTiming t = new PhoneTiming();
			t.setStart(Double.valueOf(start.split(" = ")[1]));
			t.setStop(Double.valueOf(stop.split(" = ")[1]));
			t.calculateFramesCount();
			String phone = text.split(" = ")[1].replace("\"", "");
			t.setPhone(changePhone(phone));
			output.add(t);
		}
		return output;
	}
}
package testsets.mfcc;

import io.ObjectIOManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MfccFileParser {
	private String directoryPath;
	private String dbPath;

	public MfccFileParser(String directoryPath, String dbPath) {
		this.directoryPath = directoryPath;
		this.dbPath = dbPath;
	}

	public List<MfccSample> loadSamples() throws Exception {
		List<MfccSample> output = new ArrayList<>();
		Map<String, List<String>> dictionary = loadDatabase();
		File dir = new File(directoryPath);
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				File[] mfccFiles = f.listFiles(new FileFilter() {

					@Override
					public boolean accept(File file) {
						return file.getName().endsWith(".txt");
					}
				});
				for (File mfccFile : mfccFiles) {
					MfccSample sample = new MfccSample();
					List<String> transcriptions = dictionary.get(mfccFile.getName().split("\\.")[0]);
					sample.setTranscription(transcriptions.get(0));
					sample.setPossiblePhonemes(transcriptions.subList(1, transcriptions.size()));
					List<String> lines = Files.readAllLines(mfccFile.toPath(), Charset.forName("UTF-8"));
					for (int i = 1; i < lines.size() - 2; i += 2) {
						String line = lines.get(i) + lines.get(i + 1);
						String[] split = line.split(" ");
						List<Double> mfccs = new ArrayList<>();
						for (int j = 1; j < split.length; j++) {
							String val = split[j];
							if (!val.equals(" ") && !val.equals("")) {
								mfccs.add(Double.valueOf(val));
							}
						}
						Double[] mfccArray = new Double[mfccs.size()];
						mfccArray = mfccs.toArray(mfccArray);
						MfccVector vector = new MfccVector();
						vector.setInputStates(mfccArray);
						sample.addVector(vector);
					}
					output.add(sample);
				}
			}
		}
		return output;
	}

	private Map<String, List<String>> loadDatabase() throws Exception {
		Map<String, List<String>> output = new HashMap<>();
		File db = new File(dbPath);
		if (db.getName().endsWith(".db")) {
			output = ObjectIOManager.load(db);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(db));
			String line = "";
			List<String> filesSection = new ArrayList<>();
			List<String> transcriptsSection = new ArrayList<>();
			List<String> phonemesSection = new ArrayList<>();
			int section = 0;
			while ((line = br.readLine()) != null) {
				if (line.equals("")) {
					section++;
				} else {
					if (section == 0) {
						filesSection.add(line);
					} else if (section == 1) {
						transcriptsSection.add(line);
					} else if (section == 2) {
						phonemesSection.add(line);
					}
				}
			}
			br.close();
			for (String file : filesSection) {
				List<String> transcripts = new ArrayList<>();
				String[] split = file.split(" ");
				String fileId = split[0];
				String transcriptionId = split[2];
				String transcription = "";
				for (int i = 0; i < transcriptsSection.size() && transcription.equals(""); i++) {
					String transcriptionLine = transcriptsSection.get(i);
					String[] transcriptionSplit = transcriptionLine.split(" ");
					if (transcriptionSplit[0].equals(transcriptionId)) {
						int j = 3;
						while (!transcriptionSplit[j].endsWith("\"")) {
							transcription += transcriptionSplit[j] + " ";
							j++;
						}
						transcription += transcriptionSplit[j] + " ";
						transcription = transcription.substring(1, transcription.length() - 2);
					}
				}
				transcripts.add(transcription);
				for (String phonemes : phonemesSection) {
					String phonemeTrans = "";
					String[] phonemeSplit = phonemes.split(" ");
					if (phonemeSplit[1].equals(transcriptionId)) {
						int offset = phonemeSplit[0].length() + phonemeSplit[1].length() + phonemeSplit[2].length() + 4;
						phonemeTrans = phonemes.substring(offset, phonemes.length() - 2);
						transcripts.add(phonemeTrans);
					}
				}
				output.put(fileId, transcripts);
			}
			ObjectIOManager.save(output, db);
		}
		return output;
	}
}
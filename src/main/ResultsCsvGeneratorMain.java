package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultsCsvGeneratorMain {

	public static void main(String[] args) throws Exception {
		generateCsv("E:\\Dropbox\\mfcc_new\\training");
		generateCsv("E:\\Dropbox\\mfcc_new\\testing");
	}

	private static void generateCsv(String pathName) throws Exception {
		File dir = new File(pathName);

		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".txt");
			}
		});
		List<Stat> stats = new ArrayList<>();
		for (File resultFile : files) {
			BufferedReader br = new BufferedReader(new FileReader(resultFile));
			String line = "";
			int i = 0;
			Stat stat = new Stat();
			while ((line = br.readLine()) != null) {
				if (i == 0) {
					stat.setPhone(line);
				} else {
					line = line.split(": ")[1];
					stat.addStat(Double.valueOf(line));
				}
				i++;
			}
			stats.add(stat);
			br.close();
		}
		Collections.sort(stats, new Comparator<Stat>() {
			@Override
			public int compare(Stat o1, Stat o2) {
				return o1.getPhone().compareTo(o2.getPhone());
			}
		});
		DecimalFormat format = new DecimalFormat("0.000");
		File outputCsv = new File(pathName + "\\results.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsv));
		bw.write("Fonem;Accuracy;Precision;Recall;FScore");
		bw.newLine();
		Stat allStat = null;
		for (Stat stat : stats) {
			if (stat.getPhone().equals("all")) {
				allStat = stat;
			} else {
				bw.write(stat.getPhone());
				bw.write(";");
				for (Double s : stat.getStats()) {
					bw.write(format.format(s));
					bw.write(";");
				}
				bw.newLine();
			}
		}
		bw.write(allStat.getPhone());
		bw.write(";");
		for (Double s : allStat.getStats()) {
			bw.write(format.format(s));
			bw.write(";");
		}
		bw.newLine();
		bw.flush();
		bw.close();
	}

	private static class Stat {
		private String phone;
		private List<Double> stats;

		public Stat() {
			stats = new ArrayList<>();
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public void addStat(Double stat) {
			stats.add(stat);
		}

		public List<Double> getStats() {
			return stats;
		}
	}
}
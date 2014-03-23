package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class ObjectIOManager {
	private static String savePath;
	private static String loadPath;

	public static void setSavePath(String path) {
		savePath = path;
		new File(savePath).mkdirs();
	}

	public static void setLoadPath(String path) {
		loadPath = path;
		new File(loadPath).mkdirs();
	}

	@SuppressWarnings("unchecked")
	public static <T> T load(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
		File load;
		if (loadPath != null) {
			load = new File(loadPath + File.separator + f.getName());
		} else {
			load = f;
		}
		ObjectInputStream oos = new ObjectInputStream(new FileInputStream(load));
		Object o = oos.readObject();
		oos.close();
		return (T) o;
	}

	public static void save(Object o, File f) throws IOException {
		File file = new File(savePath + File.separator + f.getName());
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		} else {
			file.mkdirs();
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(o);
		oos.close();
	}
}

package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class ObjectIOManager {

	@SuppressWarnings("unchecked")
	public static <T> T load(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream oos = new ObjectInputStream(new FileInputStream(f));
		Object o  = oos.readObject();
		oos.close();
		return (T)o;
	}
	
	public static void save(Object o, File f) throws IOException{
		f.delete();
		f.createNewFile();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(o);
		oos.close();
	}
}

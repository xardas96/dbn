package main;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mnist.MNISTReader;
import ui.TrainingPanel;

public class Main {

	public static void main(String[] args) {
//		MNISTReader reader = new MNISTReader();
//		try {
//			reader.loadImages(MNISTReader.LEARNING_DATA);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
		JFrame f = new JFrame();
		TrainingPanel panel = new TrainingPanel();
		f.setContentPane(panel);
		f.setSize(new Dimension(800, 600));
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
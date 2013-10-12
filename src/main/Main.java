package main;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ui.TrainingPanel;

public class Main {

	public static void main(String[] args) {
		// MNISTReader reader = new MNISTReader(new
		// File("mnist/train-labels-idx1-ubyte.gz"), new
		// File("mnist/train-images-idx3-ubyte.gz"));
		// if (reader.verify()) {
		// reader.createTrainingSet(100);
		// }
		// final Random rand = new Random();
		// final int numVisible = reader.getCols() * reader.getRows();
		// final int numHidden = 100;
		// RestrictedBoltzmannMachine rbm =
		// BoltzmannMachineFactory.getRestrictedBoltzmannMachine(numVisible,
		// numHidden, new LayerConnectorWeightInitializer() {
		//
		// @Override
		// public float getWeight() {
		// return (float) rand.nextGaussian();
		// // }
		// // float upper = 4.0f * (float)Math.sqrt(6.0f / (numVisible +
		// // numHidden));
		// // float lower = -upper;
		// // Random rand = new Random();
		// // float finalW = rand.nextFloat() * (upper - lower) + upper;
		// // return finalW;
		// }
		// });
		// // //
		// List<InputStateVector> training = reader.getTrainingSetItems();
		// BoltzmannMachineTrainer<RestrictedBoltzmannMachine> trainer = new
		// RestrictedBoltzmannMachineTrainer(rbm, 0.1f, 100, 0.0f);
		// trainer.setTrainingStepCompletedListener(new
		// TrainingStepCompletedListener() {
		// //
		// @Override
		// public void onTrainingStepComplete(int step, int trainingBatchSize) {
		// if (step % 100 == 0) {
		// System.out.println("step " + step + "/" + trainingBatchSize);
		// }
		// }
		//
		// @Override
		// public void onTrainingBatchComplete(int currentEpoch, float
		// currentError) {
		// System.out.println("Epoch: " + currentEpoch + ", error: " +
		// currentError);
		//
		// }
		// });
		// trainer.train(training);
		// try {
		// ObjectIOManager.save(rbm, new File("mnist.rbm"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// // try {
		// // RestrictedBoltzmannMachine rbm = ObjectIOManager.load(new
		// File("mnist.rbm"));
		// // Map<Integer, List<MNISTDigitElement>> testSet =
		// reader.getTestSet();
		// // List<InputStateVector> output = new ArrayList<>();
		// // for (List<MNISTDigitElement> element : testSet.values()) {
		// // for (MNISTDigitElement el : element) {
		// // el.binarize();
		// // output.add(el);
		// // }
		// // }
		// // int i = 0;
		// // for(InputStateVector isv : output) {
		// // rbm.testVisible(isv);
		// // float[] f = rbm.getHiddenLayerStates();
		// // BufferedImage out = new BufferedImage(reader.getCols(),
		// reader.getRows(), BufferedImage.TYPE_INT_RGB);
		// //
		// // int[] ints = new int[f.length];
		// // for(int j = 0; j<ints.length; j++) {
		// // ints[j] = f[j] == 0 ? 0 : 255;
		// // }
		// //
		// //// for(int j = 0; j<ints.length; j++) {
		// //// out.setRGB(i, j, ins[]);
		// //// }
		// //
		// // WritableRaster r = out.getRaster();
		// // r.setDataElements(0, 0, 10,10, ints);
		// //
		// // ImageIO.write(out, "png", new File(i + ".png"));
		// //
		// // i++;
		// // System.out.println(f + "");
		// //
		// // }
		// // } catch (ClassNotFoundException | IOException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // }

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
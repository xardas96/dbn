package main;

import io.ObjectIOManager;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import mnist.MNISTDigitElement;
import mnist.MNISTReader;
import mnist.MnistPanel;
import boltzmann.layers.LayerConnectorWeightInitializerFactory;
import boltzmann.machines.AdaptiveLearningFactor;
import boltzmann.machines.BoltzmannMachineTrainer;
import boltzmann.machines.TrainingStepCompletedListener;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;

public class Main {

	public static void main(String[] args) {
		final MnistPanel p = new MnistPanel();
		JFrame frame = new JFrame();
		frame.setContentPane(p);
		frame.setSize(new Dimension(800, 600));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final MNISTReader reader = new MNISTReader(new File("mnist/train-labels-idx1-ubyte.gz"), new File("mnist/train-images-idx3-ubyte.gz"));
		if (reader.verify()) {
			reader.createTrainingSet(100);
		}
		final int numVisible = reader.getCols() * reader.getRows();
		final int numHidden = 10 * 10;
		final RestrictedBoltzmannMachine rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(numVisible, numHidden, LayerConnectorWeightInitializerFactory.getGaussianWeightInitializer());
		// //
		List<InputStateVector> training = reader.getTrainingSetItems();
		BoltzmannMachineTrainer<RestrictedBoltzmannMachine> trainer = new RestrictedBoltzmannMachineTrainer(rbm, new AdaptiveLearningFactor(), 500, 0.0f);
		trainer.setTrainingStepCompletedListener(new TrainingStepCompletedListener() {
			//
			@Override
			public void onTrainingStepComplete(int step, int trainingBatchSize) {
				if (step % 100 == 0) {
					System.out.println("step " + step + "/" + trainingBatchSize);
				}
			}

			@Override
			public void onTrainingBatchComplete(int currentEpoch, float currentError) {
				List<int[]> outputs = new ArrayList<>();
				for(int i = 0; i<10; i++) {
					MNISTDigitElement test = reader.getTestItem(i);
					test.binarize();
					rbm.resetUnitStates();
					rbm.initializeVisibleLayerStates(test);
					rbm.updateHiddenUnits();
					rbm.resetVisibleStates();
					rbm.reconstructVisibleUnits();
					rbm.reconstructHiddenUnits();
					float[] vis = rbm.getVisibleLayerStates();
					int[] output = new int[vis.length];
					for (int j = 0; j < vis.length; j++) {
						output[j] = Math.round(vis[j] * 225.0f);
					}
					outputs.add(output);
				}
				p.setOutputs(outputs);
				p.repaint();
				System.out.println("Epoch: " + currentEpoch + ", error: " + currentError);

			}
		});
		trainer.train(training);
		try {
			ObjectIOManager.save(rbm, new File("mnist.rbm"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (ClassNotFoundException | InstantiationException |
		// IllegalAccessException | UnsupportedLookAndFeelException e1) {
		// JOptionPane.showMessageDialog(null, e1.getMessage());
		// }
		// JFrame f = new JFrame();
		// TrainingPanel panel = new TrainingPanel();
		// f.setContentPane(panel);
		// f.setSize(new Dimension(800, 600));
		// f.setVisible(true);
		// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
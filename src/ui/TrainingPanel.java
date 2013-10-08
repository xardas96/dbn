package ui;

import io.ObjectIOManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import ui.utils.GraphBuilder;
import ui.utils.UnitVertex;
import boltzmann.machines.TrainingStepCompletedListener;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;
import edu.uci.ics.jung.graph.Graph;

public class TrainingPanel extends JPanel {
	private static final long serialVersionUID = 8461741861458437026L;
	private JPanel presentationPanel;
	private JPanel controlPanel;
	private JButton loadNetButton;
	private JButton saveNetButton;
	private JButton trainNetButton;

	private GraphPanel graphPanel = new GraphPanel();

	private RestrictedBoltzmannMachine rbm;
	private JButton btnNewButton;

	public TrainingPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		setLayout(gridBagLayout);

		presentationPanel = new JPanel();
		presentationPanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_presentationPanel = new GridBagConstraints();
		gbc_presentationPanel.weighty = 0.1;
		gbc_presentationPanel.weightx = 0.1;
		gbc_presentationPanel.insets = new Insets(5, 5, 5, 5);
		gbc_presentationPanel.fill = GridBagConstraints.BOTH;
		gbc_presentationPanel.gridx = 0;
		gbc_presentationPanel.gridy = 0;
		add(presentationPanel, gbc_presentationPanel);
		presentationPanel.setLayout(new BorderLayout(0, 0));

		controlPanel = new JPanel();
		GridBagConstraints gbc_controlPanel = new GridBagConstraints();
		gbc_controlPanel.insets = new Insets(5, 5, 5, 5);
		gbc_controlPanel.fill = GridBagConstraints.BOTH;
		gbc_controlPanel.gridx = 1;
		gbc_controlPanel.gridy = 0;
		add(controlPanel, gbc_controlPanel);
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[] { 0, 0 };
		gbl_controlPanel.rowHeights = new int[] { 0, 0 };
		gbl_controlPanel.columnWeights = new double[] { 0.0, 0.0 };
		gbl_controlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		controlPanel.setLayout(gbl_controlPanel);

		loadNetButton = new JButton("Wczytaj");
		loadNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
				int ret = chooser.showOpenDialog(TrainingPanel.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						rbm = ObjectIOManager.load(file);
					} catch (ClassNotFoundException | IOException e1) {
						JOptionPane.showMessageDialog(TrainingPanel.this, e1.getMessage());
					}
					if (rbm != null) {
						disableAll();
						// BoltzmannMachineFactory.getRestrictedBoltzmannMachine(6, 2, 0.1f);
						graphPanel = new GraphPanel();
						Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
						graphPanel.setGraph(graph);
						graphPanel.initAndShowGraph();

						presentationPanel.add(graphPanel, BorderLayout.CENTER);
						presentationPanel.revalidate();
						presentationPanel.repaint();
						enableAll();
					}
				}
			}
		});
		GridBagConstraints gbc_loadNetButton = new GridBagConstraints();
		gbc_loadNetButton.insets = new Insets(0, 0, 5, 5);
		gbc_loadNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadNetButton.anchor = GridBagConstraints.NORTH;
		gbc_loadNetButton.gridx = 0;
		gbc_loadNetButton.gridy = 0;
		controlPanel.add(loadNetButton, gbc_loadNetButton);

		saveNetButton = new JButton("Zapisz");
		saveNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
				int ret = chooser.showSaveDialog(TrainingPanel.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						ObjectIOManager.save(rbm, file);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(TrainingPanel.this, e1.getMessage());
					}
				}
			}
		});
		GridBagConstraints gbc_saveNetButton = new GridBagConstraints();
		gbc_saveNetButton.insets = new Insets(0, 0, 5, 0);
		gbc_saveNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveNetButton.anchor = GridBagConstraints.NORTH;
		gbc_saveNetButton.gridx = 1;
		gbc_saveNetButton.gridy = 0;
		controlPanel.add(saveNetButton, gbc_saveNetButton);

		trainNetButton = new JButton("Ucz");
		trainNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Teacher().execute();

			}
		});
		GridBagConstraints gbc_trainNetButton = new GridBagConstraints();
		gbc_trainNetButton.insets = new Insets(0, 0, 0, 5);
		gbc_trainNetButton.fill = GridBagConstraints.BOTH;
		gbc_trainNetButton.gridx = 0;
		gbc_trainNetButton.gridy = 3;
		controlPanel.add(trainNetButton, gbc_trainNetButton);

		btnNewButton = new JButton("Testuj");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				InputStateVector test = new InputStateVector(new int[] { 0, 0, 0, 1, 1, 0 });
				rbm.initializeVisibleLayerStates(test);
				rbm.updateHiddenUnits();
				float[] states = rbm.getHiddenLayerStates();
				System.out.println(states[0] + ", " + states[1]);
				Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
				graphPanel.setGraph(graph);
				graphPanel.initAndShowGraph();
				graphPanel.revalidate();
				graphPanel.repaint();
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 3;
		controlPanel.add(btnNewButton, gbc_btnNewButton);

	}

	private void disableAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		saveNetButton.setEnabled(false);
		loadNetButton.setEnabled(false);
		trainNetButton.setEnabled(false);
		graphPanel.setEnabled(false);
	}

	private void enableAll() {
		setCursor(Cursor.getDefaultCursor());
		saveNetButton.setEnabled(true);
		loadNetButton.setEnabled(true);
		trainNetButton.setEnabled(true);
		graphPanel.setEnabled(true);
	}

	private class Teacher extends SwingWorker<Void, Void> {

		public Teacher() {
			disableAll();
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Void doInBackground() throws Exception {
			InputStateVector input1 = new InputStateVector(new int[] { 1, 1, 1, 0, 0, 0 });
			InputStateVector input2 = new InputStateVector(new int[] { 1, 0, 1, 0, 0, 0 });
			InputStateVector input3 = new InputStateVector(new int[] { 1, 1, 1, 0, 0, 0 });
			InputStateVector input4 = new InputStateVector(new int[] { 0, 0, 1, 1, 1, 0 });
			InputStateVector input5 = new InputStateVector(new int[] { 0, 0, 1, 1, 0, 0 });
			InputStateVector input6 = new InputStateVector(new int[] { 0, 0, 1, 1, 1, 0 });

			ArrayList<InputStateVector> learning = new ArrayList<>();
			learning.add(input1);
			learning.add(input2);
			learning.add(input3);
			learning.add(input4);
			learning.add(input5);
			learning.add(input6);

			RestrictedBoltzmannMachineTrainer trainer = new RestrictedBoltzmannMachineTrainer(rbm, 10000, 0.1f);
			trainer.setTrainingStepCompletedListener(new TrainingStepCompletedListener() {

				@Override
				public void onTrainingStepComplete() {
					// try {
					// Thread.sleep(500);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// publish();

					// TODO Auto-generated method stub

				}

				@Override
				public void onTrainingBatchComplete(int currentEpoch, float currentError) {
					// TODO Auto-generated method stub
					System.out.println("Epoch: " + currentEpoch + ", error: " + currentError);
				}
			});

			trainer.train(learning);
			return null;
		}

		@Override
		protected void process(List<Void> chunks) {

			// GraphPanel g = new GraphPanel();

			Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
			graphPanel.setGraph(graph);
			graphPanel.initAndShowGraph();
			graphPanel.revalidate();
			graphPanel.repaint();
			// presentationPanel.removeAll();
			// presentationPanel.add(g, BorderLayout.CENTER);
			// presentationPanel.revalidate();
			// presentationPanel.repaint();
		}

		@Override
		protected void done() {
			enableAll();
			super.done();
		}
	}

}

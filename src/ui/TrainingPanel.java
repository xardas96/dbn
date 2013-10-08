package ui;

import io.InputStateVectorLoader;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import ui.utils.GraphBuilder;
import ui.utils.UnitVertex;
import boltzmann.machines.TrainingStepCompletedListener;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

import javax.swing.JProgressBar;

public class TrainingPanel extends JPanel {
	private static final long serialVersionUID = 8461741861458437026L;
	private JPanel presentationPanel;
	private JPanel trainPanel;
	private JButton createNetButton;
	private JButton saveNetButton;
	private JButton trainNetButton;
	private GraphPanel graphPanel = new GraphPanel();
	private JButton loadTrainingSetButton;
	private JTabbedPane tabbedPane;
	private JPanel testPanel;
	private JButton loadNetButton;
	private JButton testNetButton;

	// logic
	private RestrictedBoltzmannMachine rbm;
	private List<InputStateVector> learningData;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JProgressBar progressBar;

	public TrainingPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
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

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.weightx = 0.1;
		gbc_tabbedPane.insets = new Insets(5, 5, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 1;
		gbc_tabbedPane.gridy = 0;
		add(tabbedPane, gbc_tabbedPane);

		trainPanel = new JPanel();
		tabbedPane.addTab("Trenowanie sieci", null, trainPanel, null);
		tabbedPane.setEnabledAt(0, true);
		GridBagLayout gbl_trainPanel = new GridBagLayout();
		gbl_trainPanel.columnWidths = new int[] { 0, 0 };
		gbl_trainPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_trainPanel.columnWeights = new double[] { 0.0, 0.0 };
		gbl_trainPanel.rowWeights = new double[] { 0.0, 0.0, 1.0 };
		trainPanel.setLayout(gbl_trainPanel);

		createNetButton = new JButton("Stw\u00F3rz sie\u0107");
		createNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO dialog konfiguracyjny
				rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(6, 2, 0.1f);
				graphPanel = new GraphPanel();
				Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
				graphPanel.setGraph(graph);
				graphPanel.initAndShowGraph(Mode.TRANSFORMING);
				presentationPanel.add(graphPanel, BorderLayout.CENTER);
				presentationPanel.revalidate();
				presentationPanel.repaint();
			}
		});
		GridBagConstraints gbc_createNetButton = new GridBagConstraints();
		gbc_createNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_createNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_createNetButton.anchor = GridBagConstraints.NORTH;
		gbc_createNetButton.gridx = 0;
		gbc_createNetButton.gridy = 0;
		trainPanel.add(createNetButton, gbc_createNetButton);

		saveNetButton = new JButton("Zapisz sie\u0107");
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
		gbc_saveNetButton.weightx = 0.2;
		gbc_saveNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveNetButton.anchor = GridBagConstraints.NORTH;
		gbc_saveNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_saveNetButton.gridx = 1;
		gbc_saveNetButton.gridy = 0;
		trainPanel.add(saveNetButton, gbc_saveNetButton);

		trainNetButton = new JButton("Ucz");
		trainNetButton.setEnabled(false);
		trainNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Teacher().execute();
			}
		});

		loadTrainingSetButton = new JButton("Wczytaj ci\u0105g ucz\u0105cy");
		loadTrainingSetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
				int ret = chooser.showOpenDialog(TrainingPanel.this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					try {
						learningData = InputStateVectorLoader.loadFromFile(f);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(TrainingPanel.this, e1.getMessage());
					}
					trainNetButton.setEnabled(learningData != null && rbm != null);
				}
			}
		});
		GridBagConstraints gbc_loadTrainingSetButton = new GridBagConstraints();
		gbc_loadTrainingSetButton.weighty = 0.1;
		gbc_loadTrainingSetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadTrainingSetButton.anchor = GridBagConstraints.NORTH;
		gbc_loadTrainingSetButton.insets = new Insets(5, 5, 5, 5);
		gbc_loadTrainingSetButton.gridx = 0;
		gbc_loadTrainingSetButton.gridy = 1;
		trainPanel.add(loadTrainingSetButton, gbc_loadTrainingSetButton);
		GridBagConstraints gbc_trainNetButton = new GridBagConstraints();
		gbc_trainNetButton.weightx = 0.2;
		gbc_trainNetButton.weighty = 0.1;
		gbc_trainNetButton.anchor = GridBagConstraints.NORTH;
		gbc_trainNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_trainNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_trainNetButton.gridx = 1;
		gbc_trainNetButton.gridy = 1;
		trainPanel.add(trainNetButton, gbc_trainNetButton);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		trainPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		panel.setLayout(gbl_panel);

		lblNewLabel = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		lblNewLabel_1 = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.anchor = GridBagConstraints.SOUTH;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.weighty = 0.1;
		gbc_progressBar.insets = new Insets(5, 5, 5, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 4;
		panel.add(progressBar, gbc_progressBar);

		testPanel = new JPanel();
		tabbedPane.addTab("Testowanie sieci", null, testPanel, null);
		GridBagLayout gbl_testPanel = new GridBagLayout();
		gbl_testPanel.columnWidths = new int[] { 0, 0 };
		gbl_testPanel.rowHeights = new int[] { 0 };
		gbl_testPanel.columnWeights = new double[] { 1.0, 1.0 };
		gbl_testPanel.rowWeights = new double[] { 0.0 };
		testPanel.setLayout(gbl_testPanel);

		loadNetButton = new JButton("Wczytaj sie\u0107");
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
						BoltzmannMachineFactory.getRestrictedBoltzmannMachine(6, 2, 0.1f);
						graphPanel = new GraphPanel();
						Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
						graphPanel.setGraph(graph);
						graphPanel.initAndShowGraph(Mode.TRANSFORMING);
						presentationPanel.add(graphPanel, BorderLayout.CENTER);
						presentationPanel.revalidate();
						presentationPanel.repaint();
						enableAll();
					}
				}
			}
		});
		GridBagConstraints gbc_loadNetButton = new GridBagConstraints();
		gbc_loadNetButton.weighty = 0.1;
		gbc_loadNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadNetButton.anchor = GridBagConstraints.NORTH;
		gbc_loadNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_loadNetButton.gridx = 0;
		gbc_loadNetButton.gridy = 0;
		testPanel.add(loadNetButton, gbc_loadNetButton);

		testNetButton = new JButton("Testuj sie\u0107");
		testNetButton.setEnabled(false);
		testNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO dialog konfiguracyjny
				InputStateVector test = new InputStateVector(new int[] { 0, 0, 0, 1, 1, 0 });
				rbm.initializeVisibleLayerStates(test);
				rbm.updateHiddenUnits();
				float[] states = rbm.getHiddenLayerStates();
				System.out.println(states[0] + ", " + states[1]);
				Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
				graphPanel.setGraph(graph);
				graphPanel.initAndShowGraph(Mode.TRANSFORMING);
				graphPanel.revalidate();
				graphPanel.repaint();
			}
		});
		GridBagConstraints gbc_testNetButton = new GridBagConstraints();
		gbc_testNetButton.weighty = 0.1;
		gbc_testNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_testNetButton.anchor = GridBagConstraints.NORTH;
		gbc_testNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_testNetButton.gridx = 1;
		gbc_testNetButton.gridy = 0;
		testPanel.add(testNetButton, gbc_testNetButton);
	}

	private void disableAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		createNetButton.setEnabled(false);
		saveNetButton.setEnabled(false);
		loadTrainingSetButton.setEnabled(false);
		trainNetButton.setEnabled(false);
		loadNetButton.setEnabled(false);
		testNetButton.setEnabled(false);
	}

	private void enableAll() {
		setCursor(Cursor.getDefaultCursor());
		createNetButton.setEnabled(true);
		saveNetButton.setEnabled(true);
		loadTrainingSetButton.setEnabled(true);
		trainNetButton.setEnabled(true);
		loadNetButton.setEnabled(true);
		testNetButton.setEnabled(true);
	}

	private class Teacher extends SwingWorker<Void, List<Number>> {
		private final static int UPDATE_NET = 0;
		private final static int UPDATE_INFO = 1;
		private RestrictedBoltzmannMachineTrainer trainer;

		public Teacher() {
			disableAll();
			trainer = new RestrictedBoltzmannMachineTrainer(rbm, 10000, 0.1f);
			progressBar.setMaximum(10000);
		}

		@Override
		protected Void doInBackground() throws Exception {
			// TODO konfiguracja trenera
			trainer.setTrainingStepCompletedListener(new TrainingStepCompletedListener() {

				@Override
				public void onTrainingStepComplete() {
					try {
						Thread.sleep(200);
					} catch (final InterruptedException e) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								JOptionPane.showMessageDialog(TrainingPanel.this, e.getMessage());
							}
						});
					}
					List<Number> list = new ArrayList<>();
					list.add(UPDATE_NET);
					publish(list);
				}

				@Override
				public void onTrainingBatchComplete(int currentEpoch, float currentError) {
					List<Number> list = new ArrayList<>();
					list.add(UPDATE_INFO);
					list.add(currentEpoch);
					list.add(currentError);
					publish(list);
				}
			});
			trainer.train(learningData);
			return null;
		}

		@Override
		protected void process(List<List<Number>> chunks) {
			for (List<Number> list : chunks) {
				int command = (Integer) list.get(0);
				switch (command) {
				case UPDATE_NET:
					Graph<UnitVertex, Float> graph = GraphBuilder.buildGraph(rbm);
					graphPanel.setGraph(graph);
					graphPanel.initAndShowGraph(Mode.PICKING);
					graphPanel.revalidate();
					graphPanel.repaint();
					break;
				case UPDATE_INFO:
					int currentEpoch = (Integer) list.get(1);
					progressBar.setValue(currentEpoch + 1);
					float currentError = (Float) list.get(2);
					lblNewLabel.setText(currentEpoch + "");
					lblNewLabel_1.setText(currentError + "");
					break;
				}
			}
		}

		@Override
		protected void done() {
			enableAll();
		}
	}
}
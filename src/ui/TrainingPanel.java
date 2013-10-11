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
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import ui.utils.GraphBuilder;
import ui.utils.UnitVertex;
import ui.utils.WeightedConnection;
import boltzmann.machines.TrainingStepCompletedListener;
import boltzmann.machines.factory.BoltzmannMachineFactory;
import boltzmann.machines.restricted.RestrictedBoltzmannMachine;
import boltzmann.machines.restricted.RestrictedBoltzmannMachineTrainer;
import boltzmann.vectors.InputStateVector;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

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
	private JButton testVisibleButton;

	// logic
	private RestrictedBoltzmannMachine rbm;
	private List<InputStateVector> learningData;
	private JPanel infoPanel;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JProgressBar progressBar;
	private JButton testHiddenButton;
	private JButton clearNetButton;

	public TrainingPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] {0};
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
		gbl_trainPanel.columnWidths = new int[] {0};
		gbl_trainPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_trainPanel.columnWeights = new double[] { 0.0 };
		gbl_trainPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0 };
		trainPanel.setLayout(gbl_trainPanel);

		createNetButton = new JButton("Stw\u00F3rz sie\u0107");
		createNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO dialog konfiguracyjny
				rbm = BoltzmannMachineFactory.getRestrictedBoltzmannMachine(6, 2, 0.1f);
				graphPanel = new GraphPanel();
				Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
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
		gbc_createNetButton.gridx = 0;
		gbc_createNetButton.gridy = 0;
		trainPanel.add(createNetButton, gbc_createNetButton);

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
		gbc_loadTrainingSetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_loadTrainingSetButton.insets = new Insets(5, 5, 5, 5);
		gbc_loadTrainingSetButton.gridx = 0;
		gbc_loadTrainingSetButton.gridy = 1;
		trainPanel.add(loadTrainingSetButton, gbc_loadTrainingSetButton);
		
				trainNetButton = new JButton("Ucz");
				trainNetButton.setEnabled(false);
				trainNetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new Teacher().execute();
					}
				});
				GridBagConstraints gbc_trainNetButton = new GridBagConstraints();
				gbc_trainNetButton.insets = new Insets(5, 5, 5, 5);
				gbc_trainNetButton.fill = GridBagConstraints.HORIZONTAL;
				gbc_trainNetButton.gridx = 0;
				gbc_trainNetButton.gridy = 2;
				trainPanel.add(trainNetButton, gbc_trainNetButton);
		
				saveNetButton = new JButton("Zapisz sie\u0107");
				saveNetButton.setEnabled(false);
				saveNetButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
						int ret = chooser.showSaveDialog(TrainingPanel.this);
						if (ret == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
							try {
								rbm.resetNetworkStates();
								ObjectIOManager.save(rbm, file);
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(TrainingPanel.this, e1.getMessage());
							}
						}
					}
				});
				GridBagConstraints gbc_saveNetButton = new GridBagConstraints();
				gbc_saveNetButton.fill = GridBagConstraints.HORIZONTAL;
				gbc_saveNetButton.anchor = GridBagConstraints.NORTH;
				gbc_saveNetButton.insets = new Insets(5, 5, 5, 5);
				gbc_saveNetButton.gridx = 0;
				gbc_saveNetButton.gridy = 3;
				trainPanel.add(saveNetButton, gbc_saveNetButton);

		infoPanel = new JPanel();
		GridBagConstraints gbc_infoPanel = new GridBagConstraints();
		gbc_infoPanel.weighty = 0.1;
		gbc_infoPanel.weightx = 0.1;
		gbc_infoPanel.insets = new Insets(5, 5, 0, 0);
		gbc_infoPanel.fill = GridBagConstraints.BOTH;
		gbc_infoPanel.gridx = 0;
		gbc_infoPanel.gridy = 4;
		trainPanel.add(infoPanel, gbc_infoPanel);
		GridBagLayout gbl_infoPanel = new GridBagLayout();
		gbl_infoPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_infoPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_infoPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_infoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		infoPanel.setLayout(gbl_infoPanel);
		
				lblNewLabel = new JLabel("");
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
				gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
				gbc_lblNewLabel.gridx = 0;
				gbc_lblNewLabel.gridy = 0;
				infoPanel.add(lblNewLabel, gbc_lblNewLabel);

		lblNewLabel_1 = new JLabel("");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		infoPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.anchor = GridBagConstraints.SOUTH;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.weighty = 0.1;
		gbc_progressBar.insets = new Insets(5, 5, 5, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 4;
		infoPanel.add(progressBar, gbc_progressBar);

		testPanel = new JPanel();
		tabbedPane.addTab("Testowanie sieci", null, testPanel, null);
		GridBagLayout gbl_testPanel = new GridBagLayout();
		gbl_testPanel.columnWidths = new int[] {0};
		gbl_testPanel.rowHeights = new int[] {0, 0, 0, 0};
		gbl_testPanel.columnWeights = new double[] { 1.0 };
		gbl_testPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
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
										Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
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
						gbc_loadNetButton.weightx = 0.2;
						gbc_loadNetButton.fill = GridBagConstraints.HORIZONTAL;
						gbc_loadNetButton.anchor = GridBagConstraints.NORTH;
						gbc_loadNetButton.insets = new Insets(5, 5, 5, 5);
						gbc_loadNetButton.gridx = 0;
						gbc_loadNetButton.gridy = 0;
						testPanel.add(loadNetButton, gbc_loadNetButton);
		
				testVisibleButton = new JButton("Testuj rozpoznanie");
				testVisibleButton.setEnabled(false);
				testVisibleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// TODO dialog konfiguracyjny
						InputStateVector test = new InputStateVector(new int[] { 0, 0, 0, 1, 1, 0 });
						rbm.testVisible(test);
						Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
						graphPanel.setGraph(graph);
						graphPanel.initAndShowGraph(Mode.TRANSFORMING);
						graphPanel.revalidate();
						graphPanel.repaint();
					}
				});
				GridBagConstraints gbc_testVisibleButton_1_1 = new GridBagConstraints();
				gbc_testVisibleButton_1_1.fill = GridBagConstraints.HORIZONTAL;
				gbc_testVisibleButton_1_1.anchor = GridBagConstraints.NORTH;
				gbc_testVisibleButton_1_1.insets = new Insets(5, 5, 5, 5);
				gbc_testVisibleButton_1_1.gridx = 0;
				gbc_testVisibleButton_1_1.gridy = 1;
				testPanel.add(testVisibleButton, gbc_testVisibleButton_1_1);
		
		testHiddenButton = new JButton("Testuj generowanie");
		GridBagConstraints gbc_testHiddenButton_1_1 = new GridBagConstraints();
		gbc_testHiddenButton_1_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_testHiddenButton_1_1.anchor = GridBagConstraints.NORTH;
		gbc_testHiddenButton_1_1.insets = new Insets(5, 5, 5, 5);
		gbc_testHiddenButton_1_1.gridx = 0;
		gbc_testHiddenButton_1_1.gridy = 2;
		testPanel.add(testHiddenButton, gbc_testHiddenButton_1_1);
		testHiddenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO dialog konfiguracyjny
				InputStateVector test = new InputStateVector(new int[] { 0, 1 });
				rbm.testHidden(test);
				Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
				graphPanel.setGraph(graph);
				graphPanel.initAndShowGraph(Mode.TRANSFORMING);
				graphPanel.revalidate();
				graphPanel.repaint();
			}
		});
		testHiddenButton.setEnabled(false);
		
		clearNetButton = new JButton("Wyczy\u015B\u0107 stan sieci");
		clearNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbm.resetNetworkStates();
				Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
				graphPanel.setGraph(graph);
				graphPanel.initAndShowGraph(Mode.TRANSFORMING);
				graphPanel.revalidate();
				graphPanel.repaint();
			}
		});
		clearNetButton.setEnabled(false);
		GridBagConstraints gbc_clearNetButton = new GridBagConstraints();
		gbc_clearNetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_clearNetButton.anchor = GridBagConstraints.NORTH;
		gbc_clearNetButton.weighty = 0.1;
		gbc_clearNetButton.insets = new Insets(5, 5, 5, 5);
		gbc_clearNetButton.gridx = 0;
		gbc_clearNetButton.gridy = 3;
		testPanel.add(clearNetButton, gbc_clearNetButton);
	}

	private void disableAll() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		createNetButton.setEnabled(false);
		saveNetButton.setEnabled(false);
		loadTrainingSetButton.setEnabled(false);
		trainNetButton.setEnabled(false);
		loadNetButton.setEnabled(false);
		testVisibleButton.setEnabled(false);
		testHiddenButton.setEnabled(false);
		clearNetButton.setEnabled(false);
	}

	private void enableAll() {
		setCursor(Cursor.getDefaultCursor());
		createNetButton.setEnabled(true);
		saveNetButton.setEnabled(true);
		loadTrainingSetButton.setEnabled(true);
		trainNetButton.setEnabled(true);
		loadNetButton.setEnabled(true);
		testVisibleButton.setEnabled(true);
		testHiddenButton.setEnabled(true);
		clearNetButton.setEnabled(true);
	}

	private class Teacher extends SwingWorker<Void, List<Number>> {
		private final static int UPDATE_NET = 0;
		private final static int UPDATE_INFO = 1;
		private RestrictedBoltzmannMachineTrainer trainer;

		public Teacher() {
			disableAll();
			trainer = new RestrictedBoltzmannMachineTrainer(rbm, 5000, 0.1f);
			progressBar.setMaximum(5000);
		}

		@Override
		protected Void doInBackground() throws Exception {
			// TODO konfiguracja trenera
			trainer.setTrainingStepCompletedListener(new TrainingStepCompletedListener() {

				@Override
				public void onTrainingStepComplete() {
//					try {
//						Thread.sleep(200);
//					} catch (final InterruptedException e) {
//						SwingUtilities.invokeLater(new Runnable() {
//
//							@Override
//							public void run() {
//								JOptionPane.showMessageDialog(TrainingPanel.this, e.getMessage());
//							}
//						});
//					}
//					List<Number> list = new ArrayList<>();
//					list.add(UPDATE_NET);
//					publish(list);
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
					Graph<UnitVertex, WeightedConnection> graph = GraphBuilder.buildGraph(rbm);
					graphPanel.setGraph(graph);
					graphPanel.initAndShowGraph(Mode.PICKING);
					graphPanel.revalidate();
					graphPanel.repaint();
					break;
				case UPDATE_INFO:
					int currentEpoch = (Integer) list.get(1);
					progressBar.setValue(currentEpoch + 1);
					float currentError = (Float) list.get(2);
					lblNewLabel.setText((currentEpoch + 1) + "");
					lblNewLabel_1.setText(currentError + "");
					break;
				}
			}
		}

		@Override
		protected void done() {
			rbm.resetNetworkStates();
			enableAll();
		}
	}
}
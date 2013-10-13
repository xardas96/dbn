package dbn;

import java.util.ArrayList;
import java.util.List;

import boltzmann.layers.Layer;
import boltzmann.layers.LayerConnector;
import boltzmann.layers.LayerConnectorWeightInitializer;
import boltzmann.machines.BoltzmannMachine;

public class DeepBeliefNetwork {
	private BoltzmannMachine[] boltzmannMachines;
	private List<LayerConnector> intermachineConnections;

	public DeepBeliefNetwork(BoltzmannMachine[] boltzmannMachines, LayerConnectorWeightInitializer intermachineConnectionsWeightInitializer) {
		this.boltzmannMachines = boltzmannMachines;
		intermachineConnections = new ArrayList<>();
		for (int i = 0; i < boltzmannMachines.length - 1; i++) {
			Layer lastLayer = boltzmannMachines[i].getLastLayer();
			Layer firstLayer = boltzmannMachines[i + 1].getFirstLayer();
			intermachineConnections.add(new LayerConnector(lastLayer, firstLayer, intermachineConnectionsWeightInitializer));
		}
	}
}
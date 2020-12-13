package Agents;
import jade.core.Agent;

public class Estacao extends Agent {

	protected void setup() {
		System.out.println("Estacao Agent connecting...");
		System.out.println("My name is "+ getLocalName());
	}

	protected void takeDown() {
		super.takeDown();
	}

}
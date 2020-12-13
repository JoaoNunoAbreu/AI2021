package Agents;
import jade.core.Agent;

public class Utilizador extends Agent {

	protected void setup() {
		System.out.println("Utilizador Agent connecting...");
		System.out.println("My name is "+ getLocalName());
	}

	protected void takeDown() {
		super.takeDown();
	}


}

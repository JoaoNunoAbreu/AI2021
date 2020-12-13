package Agents;
import jade.core.Agent;

public class Central extends Agent {

	protected void setup() {
		System.out.println("Central Agent connecting...");
		System.out.println("My name is "+ getLocalName());
	}

	protected void takeDown() {
		super.takeDown();
	}

}
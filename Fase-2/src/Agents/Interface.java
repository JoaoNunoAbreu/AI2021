package Agents;

import jade.core.Agent;

public class Interface extends Agent {
    protected void setup() {
        System.out.println("Interface Agent connecting...");
        System.out.println("My name is "+ getLocalName());
    }

    protected void takeDown() {
        super.takeDown();
    }
}

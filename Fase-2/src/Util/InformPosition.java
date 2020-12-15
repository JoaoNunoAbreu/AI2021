package Util;

import jade.core.AID;

public class InformPosition implements java.io.Serializable {

    private AID agent;
    private Posicao position;

    public InformPosition(AID agent, Posicao position) {
        super();
        this.agent = agent;
        this.position = position;
    }

    public InformPosition(AID agent, int x, int y) {
        super();
        this.agent = agent;
        this.position = new Posicao(x,y);
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public Posicao getPosition() {
        return position;
    }

    public void setPosition(Posicao position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "InformPosition{" +
                "agent=" + agent +
                ", position=" + position +
                '}';
    }
}
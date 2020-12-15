package Util;

import jade.core.AID;

public class MakeRequest implements java.io.Serializable {

    private AID agent;
    private Posicao init, dest;

    public MakeRequest(AID agent, Posicao init, Posicao dest) {
        super();
        this.agent = agent;
        this.init = init;
        this.dest = dest;
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public Posicao getInit() {
        return init;
    }

    public void setInit(Posicao init) {
        this.init = init;
    }

    public Posicao getDest() {
        return dest;
    }

    public void setDest(Posicao dest) {
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "MakeRequest [agent=" + agent + ", init=" + init + ", dest=" + dest + "]";
    }

}
package Util;

import jade.core.AID;

public class InfoEstacao implements java.io.Serializable {

    private AID agent;
    private Posicao position;
    private int num_bicicletas;
    private int raio;
    private int num_bicicletas_max;
    private int nRequisicoes;
    private int nDevolucoes;

    public InfoEstacao(AID agent, Posicao position, int num_bicicletas, int num_bicicletas_max, int r) {
        super();
        this.agent = agent;
        this.position = position;
        this.num_bicicletas = num_bicicletas;
        this.num_bicicletas_max = num_bicicletas_max;
        this.nRequisicoes=0;
        this.nDevolucoes=0;
        this.raio = r;
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public int getNumEstacao(){
        return Integer.parseInt(this.agent.getLocalName().substring(this.agent.getLocalName().length() - 1));
    }

    public Posicao getPosition() {
        return position;
    }

    public void setPosition(Posicao position) {
        this.position = position;
    }

    public int getNum_bicicletas() {
        return num_bicicletas;
    }

    public void setNum_bicicletas(int num_bicicletas) {
        this.num_bicicletas = num_bicicletas;
    }

    public int getRaio() {
        return raio;
    }

    public int getNum_bicicletas_max() {
        return num_bicicletas_max;
    }

    public void decrement(){
        this.num_bicicletas--;
    }

    public void increment(){
        this.num_bicicletas++;
    }

    public void incrementNRequesicoes() {this.nRequisicoes++;}

    public void incrementNDevolucoes() {this.nDevolucoes++;}

    public int getnRequisicoes() {
        return nRequisicoes;
    }

    public int getnDevolucoes() {
        return nDevolucoes;
    }

    public boolean isInside(Posicao p){
        return position.distanceBetween(p) < raio;
    }

    public float incentivo(){
        float res = 1 - (float) num_bicicletas / num_bicicletas_max;
        if(res < 1./3)
            res = 0;
        return res;
    }

    @Override
    public String toString() {
        return "InfoEstacao{" +
                "agent=" + agent.getLocalName() +
                ", position=" + position.toString() +
                ", num_bicicletas=" + num_bicicletas +
                ", raio=" + raio +
                ", num_bicicletas_max=" + num_bicicletas_max +
                '}';
    }
}
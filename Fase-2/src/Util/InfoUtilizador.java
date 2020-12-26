package Util;

import jade.core.AID;

public class InfoUtilizador implements java.io.Serializable {

    private AID agent;
    private Posicao init, dest, atual;
    private Posicao dest_original;
    private float distance34;
    private float incentivo_max;

    public InfoUtilizador(AID agent, Posicao init, Posicao dest) {
        super();
        this.agent = agent;
        this.init = init;
        this.dest = dest;
        this.atual = init;
        this.distance34 = init.distanceBetween(dest) * 3/4;
        this.dest_original = this.dest;
        this.incentivo_max = 0;
    }

    public InfoUtilizador(InfoUtilizador i) {
        this.agent = i.getAgent();
        this.init = i.getInit();
        this.dest = i.getDest();
        this.atual = i.getAtual();
        this.dest_original = i.getDest_original();
        this.distance34 = i.getDistance34();
        this.incentivo_max = i.getIncentivo_max();
    }

    public AID getAgent() {
        return agent;
    }

    public int getNumUser(){
        return Integer.parseInt(this.agent.getLocalName().substring(this.agent.getLocalName().length() - 1));
    }

    public Posicao getInit() {
        return init;
    }

    public Posicao getDest() {
        return dest;
    }

    public Posicao getAtual() {
        return atual;
    }

    public Posicao getDest_original() {
        return dest_original;
    }

    public float getDistance34() {
        return distance34;
    }

    public float getIncentivo_max() {
        return incentivo_max;
    }

    public void setAtual(Posicao atual) {
        this.atual = atual;
    }

    public void setDest(Posicao dest) {
        this.dest = dest;
    }

    public float distanciaPercorrida(){
        return init.distanceBetween(atual);
    }

    public void atualiza(){
        float v_x = dest.getX() - atual.getX();
        float v_y = dest.getY() - atual.getY();
        float distancia = atual.distanceBetween(dest);
        v_x /= distancia;
        v_y /= distancia;
        setAtual(new Posicao(atual.getX() + v_x, atual.getY() + v_y));
    }

    public boolean aceitaIncentivo(Incentivo i){
        if((i.getIncentivo() < 0.60 || i.getPosition().distanceBetween(dest_original) > 60 || i.getIncentivo() <= incentivo_max)) return false;
        incentivo_max = i.getIncentivo();
        this.setDest(i.getPosition());
        return true;
    }

    @Override
    public String toString() {
        return "\n\tInfoUtilizador{" +
                "\n\t\tagent=" + agent.getLocalName() +
                ",\n\t\t init=" + init +
                ",\n\t\t dest=" + dest +
                ",\n\t\t dest_original=" + dest_original +
                ",\n\t\t atual=" + atual +
                ",\n\t\t incentivo_max=" + incentivo_max +
                "\n\t}";
    }
}
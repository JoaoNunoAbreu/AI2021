package Util;

import Agents.Estacao;

import java.util.*;

public class Mapa {

    // Tamanho do mapa
    private int size;

    // Número de estações no mapa
    private int num_estacoes;

    // Número máximo de bicicletas permitidas nas estações
    private int num_max_bicicletas;

    // Número inicial de bicicletas nas estações
    private int num_init_bicicletas;

    // Map estação -> nº bicicletas
    private List<InfoEstacao> estacoes;

    // Lista de utilizadores
    private List<InfoUtilizador> utilizadores;

    public Mapa(int size, int num_estacoes, int num_max_bicicletas) {
        this.size = size;
        this.num_estacoes = num_estacoes;
        this.num_max_bicicletas = num_max_bicicletas;
        this.num_init_bicicletas = num_max_bicicletas / 2;
        this.estacoes = new ArrayList<>();
        this.utilizadores = new ArrayList<>();
    }

    public int getSize() {
        return size;
    }

    public List<Posicao> getEstacoes() {
        List<Posicao> res = new ArrayList<>();

        for(InfoEstacao i: this.estacoes)
            res.add(i.getPosition());

        return res;
    }

    public int getNum_init_bicicletas() {
        return num_init_bicicletas;
    }

    public int getNum_max_bicicletas() {
        return num_max_bicicletas;
    }

    public void addNewEstacao(InfoEstacao p){
        this.estacoes.add(p);
    }

    public void addUtilizadores(InfoUtilizador i){
        this.utilizadores.add(i);
    }

    public void atualizaUtilizador(InfoUtilizador iu){
        for(int i = 0; i < utilizadores.size(); i++){
            if(utilizadores.get(i).getAgent().equals(iu.getAgent())){
                utilizadores.set(i,new InfoUtilizador(iu));
            }
        }
    }

    public void decrementaBicicleta(InfoUtilizador i){
        for(InfoEstacao e : estacoes){
            if(e.getPosition().equals(i.getInit())){
                e.decrement();
            }
        }
    }

    public void incrementaBicicleta(InfoUtilizador i){
        for(InfoEstacao e : estacoes){
            if(e.getPosition().equals(i.getDest())){
                e.increment();
            }
        }
    }

    public List<InfoEstacao> getEstacoesIn(Posicao p){
        List<InfoEstacao> res = new ArrayList<>();
        for(InfoEstacao ie : estacoes)
            if(ie.isInside(p))
                res.add(ie);
        return res;
    }

    @Override
    public String toString() {
        return "Mapa{\n" +
                " size = " + size +
                ",\n num_estacoes = " + num_estacoes +
                ",\n num_max_bicicletas = " + num_max_bicicletas +
                ",\n num_init_bicicletas = " + num_init_bicicletas +
                ",\n estacoes = " + estacoes.toString() +
                ",\n utilizadores = " + utilizadores.toString() +
                "\n}";
    }
}

package Util;

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
    private Map<Posicao,Integer> estacoes;

    // Lista de utilizadores
    private List<Posicao> utilizadores;

    public Mapa(int size, int num_estacoes, int num_max_bicicletas) {
        this.size = size;
        this.num_estacoes = num_estacoes;
        this.num_max_bicicletas = num_max_bicicletas;
        this.num_init_bicicletas = this.num_max_bicicletas/2;
        this.estacoes = new HashMap<>();
        this.utilizadores = new ArrayList<>();
    }

    public int getSize() {
        return size;
    }

    public List<Posicao> getEstacoes() {
        return new ArrayList<>(this.estacoes.keySet());
    }

    public int getNum_init_bicicletas() {
        return num_init_bicicletas;
    }

    public void addNewEstacao(Posicao p){
        this.estacoes.put(p,num_init_bicicletas);
    }
    
    @Override
    public String toString() {
        return "Mapa{" +
                "size=" + size +
                ", num_estacoes=" + num_estacoes +
                ", num_max_bicicletas=" + num_max_bicicletas +
                ", num_init_bicicletas=" + num_init_bicicletas +
                ", estacoes=" + estacoes.toString() +
                ", utilizadores=" + utilizadores.toString() +
                '}';
    }
}

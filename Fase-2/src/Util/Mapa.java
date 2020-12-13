package Util;

import java.util.*;

public class Mapa {

    // Tamanho do mapa
    private int size;

    // Número de estações no mapa
    private int num_estacoes;

    // Número de utilizadores no mapa
    private int num_utilizadores;

    // Número máximo de bicicletas permitidas nas estações
    private int num_max_bicicletas;

    // Map estação -> nº bicicletas
    private Map<Posicao,Integer> estacoes;

    // Lista de utilizadores
    private List<Posicao> utilizadores;

    public Mapa(int size, int num_estacoes, int num_utilizadores, int num_max_bicicletas) {
        this.size = size;
        this.num_estacoes = num_estacoes;
        this.num_utilizadores = num_utilizadores;
        this.num_max_bicicletas = num_max_bicicletas;
        this.estacoes = new HashMap<>();
        this.utilizadores = new ArrayList<>();
    }

    public Posicao getRandPosition() {
        Random rand = new Random();
        float x = rand.nextInt(size);
        float y = rand.nextInt(size);
        return new Posicao(x,y);
    }

    private boolean demasiadoJuntos(Posicao posicao) {
        int proximidade = 5;
        for(Posicao p : this.estacoes.keySet()){
            if(Posicao.distanceBetween(posicao, p) < proximidade)
                return true;
        }
        return false;
    }

    public void geraPosEstacoes(){
        int tentativas = 10;
        Posicao p = null;
        for(int i = 0; i < num_estacoes; i++){
            boolean valid = false;
            for(int j = 0; j < tentativas && !valid; j++){
                p = getRandPosition();
                if(!demasiadoJuntos(p))
                    valid = true;
            }
            if(!valid)
                System.out.println("Não foi possível criar uma estação.");
            else
                this.estacoes.put(p,0);
        }
    }

    @Override
    public String toString() {
        return "Mapa{" +
                "size=" + size +
                ", num_estacoes=" + num_estacoes +
                ", num_utilizadores=" + num_utilizadores +
                ", num_max_bicicletas=" + num_max_bicicletas +
                ", estacoes=" + estacoes.toString() +
                ", utilizadores=" + utilizadores.toString() +
                '}';
    }
}

package Util;
import java.io.Serializable;

public class Posicao implements Serializable {
    private float x;
    private float y;

    public Posicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float distanceBetween(Posicao p2){
        return (float) Math.sqrt(((Math.pow((x - p2.x), 2)) + (Math.pow((y - p2.y), 2))));
    }

    @Override
    public boolean equals(Object p){
        if(p instanceof Posicao){
            Posicao pos = (Posicao) p;
            return Math.abs(this.x-pos.x) < 1 && Math.abs(this.y-pos.y) < 1;
        }
        else return false;
    }

    @Override
    public String toString() {
        return "("+x +"," + y +")";
    }
}

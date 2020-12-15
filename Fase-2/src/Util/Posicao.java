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

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public double distanceBetween(Posicao p2){
        return Math.sqrt(((Math.pow((x - p2.x), 2)) + (Math.pow((y - p2.y), 2))));
    }

    @Override
    public boolean equals(Object p){
        if(p instanceof Posicao){
            Posicao pos = (Posicao) p;
            return this.x == pos.x && this.y == pos.y;
        }
        else return false;
    }

    @Override
    public String toString() {
        return "("+x +"," + y +")";
    }
}

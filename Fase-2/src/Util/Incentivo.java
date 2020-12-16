package Util;

public class Incentivo implements java.io.Serializable {
    private Posicao position;
    private float incentivo;

    public Incentivo(Posicao position, float incentivo) {
        this.position = position;
        this.incentivo = incentivo;
    }

    public Posicao getPosition() {
        return position;
    }

    public float getIncentivo() {
        return incentivo;
    }

    public void setPosition(Posicao position) {
        this.position = position;
    }

    public void setIncentivo(float incentivo) {
        this.incentivo = incentivo;
    }
}

package casillas;

import objetos.Pista;
import objetos.Reliquia;

public class Templo extends Casilla {

    private Reliquia reliquia;
    private Pista pista;
    private boolean explorado = false;

    public Templo(String nombreCasilla, char simbolo, int fila, int columna, Reliquia reliquia, Pista pista, boolean explorado) {
        super(nombreCasilla, simbolo, fila, columna);
        this.reliquia = reliquia;
        this.pista = pista;
        this.explorado = explorado;
    }

    public Reliquia getReliquia() {
        return reliquia;
    }

    public void setReliquia(Reliquia reliquia) {
        this.reliquia = reliquia;
    }

    public Pista getPista() {
        return pista;
    }

    public void setPista(Pista pista) {
        this.pista = pista;
    }

    public boolean isExplorado() {
        return explorado;
    }

    public void setExplorado(boolean explorado) {
        this.explorado = explorado;
    }

    @Override
    public  int obtenerCostoMovimiento(){
        return 1;
    }

}

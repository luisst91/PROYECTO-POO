package casillas;

public abstract class Casilla {

    protected String nombreCasilla;
    protected char simbolo;
    protected int fila;
    protected int columna;

    public Casilla(String nombreCasilla, char simbolo, int fila, int columna) {
        this.nombreCasilla = nombreCasilla;
        this.simbolo = simbolo;
        this.fila = fila;
        this.columna = columna;
    }

    public String getNombreCasilla() {
        return nombreCasilla;
    }

    public void setNombreCasilla(String nombreCasilla) {
        this.nombreCasilla = nombreCasilla;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(char simbolo) {
        this.simbolo = simbolo;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public abstract int obtenerCostoMovimiento();
}

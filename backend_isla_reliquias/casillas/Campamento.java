package casillas;

public class Campamento extends Casilla {
    public Campamento(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }
    @Override
    public int obtenerCostoMovimiento(){
        return 1;
    }

}

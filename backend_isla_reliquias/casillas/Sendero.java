package casillas;

public class Sendero extends Casilla {

    public Sendero(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }
    @Override
    public  int obtenerCostoMovimiento(){
        return 1;
    }
}

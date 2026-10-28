package casillas;

public class Cofre extends Casilla {


    public Cofre(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }
    @Override
    public  int obtenerCostoMovimiento(){
        return 1;
    }
}
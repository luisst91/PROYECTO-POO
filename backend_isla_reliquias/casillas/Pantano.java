package casillas;

public class Pantano extends Casilla{
    public Pantano(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }
    @Override
    public  int obtenerCostoMovimiento(){
        return 2;
    }
}

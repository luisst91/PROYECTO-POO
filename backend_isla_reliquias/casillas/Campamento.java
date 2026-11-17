package casillas;
/**
 * Representa la casilla de Campamento dentro del tablero de juego.
 * Es el punto de partida de los jugadores y el único lugar donde pueden entregar
 * las reliquias recolectadas para sumar puntos de victoria.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Campamento extends Casilla {
    /**
     * Construye una nueva instancia de un Campamento en las coordenadas especificadas.
     *
     * @param nombreCasilla El nombre identificativo de la casilla (por ejemplo, "Campamento").
     * @param simbolo       El carácter que representa al campamento en el mapa ({@code 'C'}).
     * @param fila          La coordenada de fila dentro del tablero.
     * @param columna       La coordenada de columna dentro del tablero.
     */
    public Campamento(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    } 
    /**
     * Obtiene el costo de movimiento necesario para ingresar a la casilla de Campamento.
     *
     * @return {@code 1} punto de acción.
     */
    @Override
    public int obtenerCostoMovimiento() {
        return 1;
    }
}

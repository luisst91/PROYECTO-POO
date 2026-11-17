package casillas;

/**
 * Representa la casilla de Pantano dentro del tablero de juego.
 * Es un terreno difícil de atravesar que requiere un mayor consumo de puntos de acción
 * en comparación con los senderos normales.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Pantano extends Casilla{

    /**
     * Construye una nueva instancia de un Pantano en las coordenadas especificadas.
     *
     * @param nombreCasilla El nombre identificativo de la casilla
     * @param simbolo       El carácter que representa al pantano en el mapa
     * @param fila          La coordenada de fila dentro del tablero.
     * @param columna       La coordenada de columna dentro del tablero.
     */
    public Pantano(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }

    /**
     * Obtiene el costo de movimiento necesario para ingresar a la casilla de Pantano.
     *
     * @return {@code 2} puntos de acción debido a la dificultad del terreno.
     */
    @Override
    public int obtenerCostoMovimiento() {
        return 2;
    }
}

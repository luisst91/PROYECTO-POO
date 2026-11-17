package casillas;

/**
 * Casilla de terreno normal (símbolo S) por la que los jugadores pueden caminar sin ningún efecto especial.
 * Es el tipo de casilla más común del tablero: hay 15 de las 25 casillas.
 *
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class Sendero extends Casilla {

    /**
     * Construye un sendero llamando al constructor de {@link Casilla} con sus datos básicos.
     *
     * @param nombreCasilla Nombre que se muestra del terreno ("Sendero")
     * @param simbolo Letra que lo representa en el tablero ('S')
     * @param fila Fila donde se ubica la casilla
     * @param columna Columna donde se ubica la casilla
     */
    public Sendero(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }
}

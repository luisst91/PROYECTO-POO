package casillas;

/**
 * Representa la casilla del Cofre misterioso dentro del tablero de juego.
 * Contiene una reliquia especial de alto valor que solo puede ser obtenida
 * tras reunir las pistas en los templos y descifrar su contraseña.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Cofre extends Casilla {
    /**
     * Indica si el cofre ya fue abierto por algún jugador ({@code true}) o si permanece cerrado ({@code false}).
     */
    private boolean abierto = false;

    /**
     * Construye una nueva instancia de un Cofre en las coordenadas especificadas.
     *
     * @param nombreCasilla El nombre identificativo de la casilla (por ejemplo, "Cofre").
     * @param simbolo       El carácter que representa al cofre en el mapa ({@code 'X'}).
     * @param fila          La coordenada de fila dentro del tablero.
     * @param columna       La coordenada de columna dentro del tablero.
     */
    public Cofre(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
    }

    /**
     * Verifica si el cofre ya ha sido abierto.
     *
     * @return {@code true} si el cofre está abierto; {@code false} si aún sigue cerrado.
     */
    public boolean isAbierto() {
        return abierto;
    }

    /**
     * Actualiza el estado de apertura del cofre.
     *
     * @param abierto {@code true} para marcar el cofre como abierto; {@code false} en caso contrario.
     */
    public void setAbierto(boolean abierto) {
        this.abierto = abierto;
    }

    /**
     * Obtiene el costo de movimiento necesario para ingresar a la casilla del Cofre.
     *
     * @return {@code 1} punto de acción.
     */
    @Override
    public int obtenerCostoMovimiento() {
        return 1;
    }

}

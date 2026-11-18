package casillas;

/**
 * Casilla de pantano (símbolo P). Entrar a ella cuesta más puntos de acción que a una casilla normal.
 *
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class Pantano extends Casilla{

    /**
     * Costo de entrar a un pantano. No tiene un valor fijo: se coloca con {@link #setCostoPantano(int)}
     * con el valor que entrega la API.
     */
    private static int costoPantano;

    /**
     * Construye un pantano y le asigna el costo de movimiento de los pantanos.
     *
     * @param nombreCasilla Nombre que se muestra del terreno ("Pantano")
     * @param simbolo Letra que lo representa en el tablero ('P')
     * @param fila Fila donde se ubica la casilla
     * @param columna Columna donde se ubica la casilla
     */
    public Pantano(String nombreCasilla, char simbolo, int fila, int columna) {
        super(nombreCasilla, simbolo, fila, columna);
        this.costoCasilla = costoPantano;
    }

    /**
     * Devuelve el costo de entrar a un pantano.
     *
     * @return Los puntos de acción que cuesta entrar a un pantano
     */
    public static int getCostoPantano() {
        return costoPantano;
    }

    /**
     * Coloca el costo de entrar a un pantano. Se debe llamar antes de crear el tablero.
     *
     * @param nuevoCosto Nuevo costo que entrega la API
     */
    public static void setCostoPantano(int nuevoCosto) {
        costoPantano = nuevoCosto;
    }
}

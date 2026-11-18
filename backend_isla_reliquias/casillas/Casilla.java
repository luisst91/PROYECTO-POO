package casillas;

/**
 * Clase base de todas las casillas del tablero (Campamento, Cofre, Templo, Pantano y Sendero).
 * Guarda el nombre, el símbolo, la posición y el costo en puntos de acción de entrar a la casilla.
 *
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public abstract class Casilla {

    /**
     * Costo de entrar a una casilla normal. No tiene un valor fijo: se coloca con {@link #setCostoNormal(int)}
     * con el valor que entrega la API.
     */
    private static int costoNormal;

    protected String nombreCasilla;
    protected char simbolo;
    protected int fila;
    protected int columna;
    /**
     * Puntos de acción que cuesta entrar a esta casilla. Se fija al crearla.
     */
    protected int costoCasilla;

    /**
     * Construye una casilla. Su costo es el de una casilla normal; las subclases
     * con un costo distinto (como {@link Pantano}) lo cambian en su propio constructor.
     *
     * @param nombreCasilla Nombre que se muestra del terreno
     * @param simbolo Letra que lo representa en el tablero
     * @param fila Fila donde se ubica la casilla
     * @param columna Columna donde se ubica la casilla
     */
    public Casilla(String nombreCasilla, char simbolo, int fila, int columna) {
        this.nombreCasilla = nombreCasilla;
        this.simbolo = simbolo;
        this.fila = fila;
        this.columna = columna;
        this.costoCasilla = costoNormal;
    }

    /**
     * Devuelve el costo de entrar a una casilla normal.
     *
     * @return Los puntos de acción que cuesta entrar a una casilla normal
     */
    public static int getCostoNormal() {
        return costoNormal;
    }

    /**
     * Coloca el costo de entrar a una casilla normal. Se debe llamar antes de crear el tablero.
     *
     * @param nuevoCosto Nuevo costo que entrega la API
     */
    public static void setCostoNormal(int nuevoCosto) {
        costoNormal = nuevoCosto;
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

    /**
     * Devuelve los puntos de acción que cuesta moverse a esta casilla.
     *
     * @return El costo de movimiento de la casilla
     */
    public int obtenerCostoMovimiento() {
        return costoCasilla;
    }

}

package casillas;

/**
 * Representa una casilla abstracta genérica dentro del tablero de juego.
 * Contiene las propiedades básicas como nombre, símbolo y ubicación (fila y columna),
 * además de definir el método abstracto para obtener el costo de movimiento de cada terreno.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public abstract class Casilla {
    /**
     * Nombre identificativo del tipo de casilla (por ejemplo: "Campamento", "Templo").
     */
    protected String nombreCasilla;

    /**
     * Carácter que representa visualmente a la casilla en el mapa.
     */
    protected char simbolo;

    /**
     * Coordenada de fila donde se ubica la casilla en el tablero.
     */
    protected int fila;

    /**
     * Coordenada de columna donde se ubica la casilla en el tablero.
     */
    protected int columna;

    /**
     * Costo asociado a la casilla.
     */
    protected int costoCasilla;

    /**
     * Construye una nueva instancia de una Casilla con sus atributos espaciales e identificativos.
     *
     * @param nombreCasilla El nombre que identifica a la casilla.
     * @param simbolo       El carácter que representa a la casilla en el tablero.
     * @param fila          La coordenada de fila dentro del tablero.
     * @param columna       La coordenada de columna dentro del tablero.
     */
    public Casilla(String nombreCasilla, char simbolo, int fila, int columna) {
        this.nombreCasilla = nombreCasilla;
        this.simbolo = simbolo;
        this.fila = fila;
        this.columna = columna;
    }

    /**
     * Obtiene el nombre identificativo de la casilla.
     *
     * @return El nombre de la casilla.
     */
    public String getNombreCasilla() {
        return nombreCasilla;
    }

    /**
     * Establece el nombre de la casilla.
     *
     * @param nombreCasilla El nuevo nombre para la casilla.
     */
    public void setNombreCasilla(String nombreCasilla) {
        this.nombreCasilla = nombreCasilla;
    }

    /**
     * Obtiene el símbolo que representa a la casilla en el tablero.
     *
     * @return El carácter símbolo de la casilla.
     */
    public char getSimbolo() {
        return simbolo;
    }

    /**
     * Establece el símbolo de la casilla.
     *
     * @param simbolo El nuevo carácter símbolo.
     */
    public void setSimbolo(char simbolo) {
        this.simbolo = simbolo;
    }

    /**
     * Obtiene el índice de la fila donde se encuentra la casilla.
     *
     * @return La coordenada de fila.
     */
    public int getFila() {
        return fila;
    }

    /**
     * Establece la coordenada de fila de la casilla.
     *
     * @param fila El nuevo índice de fila.
     */
    public void setFila(int fila) {
        this.fila = fila;
    }

    /**
     * Obtiene el índice de la columna donde se encuentra la casilla.
     *
     * @return La coordenada de columna.
     */
    public int getColumna() {
        return columna;
    }

    /**
     * Establece la coordenada de columna de la casilla.
     *
     * @param columna El nuevo índice de columna.
     */
    public void setColumna(int columna) {
        this.columna = columna;
    }

    /**
     * Obtiene el costo de movimiento necesario para que un jugador ingrese a esta casilla.
     * Cada subclase debe implementarlo según el tipo de terreno.
     *
     * @return La cantidad de puntos de acción requeridos para entrar en la casilla.
     */
    public abstract int obtenerCostoMovimiento();
}

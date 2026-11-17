package jugador;

import casillas.Casilla;
import objetos.Reliquia;

/**
 * Representa a un jugador dentro de la partida
 * Administra su posicion actual en el tablero, su inventario de reliquias cargadas,
 * el puntaje acumulado y las estadisticas de reliquias entregadas
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Jugador {
    /**
     * Capacidad maxima de reliquias que un jugador puede transportar simultaneamente en su inventario
     */
    public static final int CAPACIDAD_RELIQUIAS = 2;

    /**
     * Nombre de usuario unico que identifica al jugador durante la partida
     */
    private String nombreUsuario;
    /**
     * Coordenada de la fila actual en la que se encuentra el jugador dentro del tablero
     */
    private int filaActual;
    /**
     * Coordenada de la columna actual en la que se encuentra el jugador dentro del tablero
     */
    private int columnaActual;
    /**
     * Arreglo que almacena las reliquias que el jugador lleva en su inventario
     */
    private Reliquia[] inventarioReliquias = new Reliquia[CAPACIDAD_RELIQUIAS];
    /**
     * Cantidad de reliquias que el jugador lleva actualmente en su inventario
     */
    private int cantidadReliquias = 0;
    /**
     * Total de puntos acumulados por el jugador al entregar reliquias
     */
    private int puntos = 0;
    /**
     * Contador historico de reliquias entregadas por el jugador en el campamento
     */
    private int reliquiasEntregadas = 0;

    /**
     * Contruye una nueva instancia de un jugador con su nombre de usuario y sus coordenadas iniciales dentro del tablero
     * @param nombreUsuario El nombre identificativo del jugador.
     * @param filaActual La fila inicial donde comienza el jugador en el tablero.
     * @param columnaActual La columna inicial donde comienza el jugador en el tablero.
    */

    public Jugador(String nombreUsuario, int filaActual, int columnaActual) {
        this.nombreUsuario = nombreUsuario;
        this.filaActual = filaActual;
        this.columnaActual = columnaActual;
    }

    // true si la casilla esta justo arriba, abajo, a la izquierda o a la derecha

    /**
     * Verifica si una casilla destino está justo al lado del jugador
     * (arriba, abajo, izquierda o derecha, sin contar diagonales)
     * 
     * @param destino La distancia de {@link Casilla} que se desea evaluar
     * @return {@code true} si la casilla está exactamente a 1 paso ortogonal de distancia;{@code false} en caso contrario
     *      
     */
    public boolean esVecina(Casilla destino) {
        int diferenciaFila = destino.getFila() - filaActual;
        int diferenciaColumna = destino.getColumna() - columnaActual;
        if (diferenciaFila < 0) {
            diferenciaFila = -diferenciaFila;
        }
        if (diferenciaColumna < 0) {
            diferenciaColumna = -diferenciaColumna;
        }
        return diferenciaFila + diferenciaColumna == 1;
    }
    /**
     * Actualiza las coordenadas espaciales del jugador trasladándolo a la ubicación de la casilla destino.
     *
     * @param destino La instancia de {@link Casilla} a la que se desplazará el jugador.
     */

    public void moverJugador(Casilla destino) {
        filaActual = destino.getFila();
        columnaActual = destino.getColumna();
    }

    /**
     * Verifica si el inventario del jugador aún tiene espacio disponible para recoger otra reliquia.
     *
     * @return {@code true} si {@link #cantidadReliquias} es menor que {@link #CAPACIDAD_RELIQUIAS}; {@code false} si está lleno.
     */
    public boolean tieneEspacio() {
        return cantidadReliquias < CAPACIDAD_RELIQUIAS;
    }

    /**
     * Comprueba si el jugador transporta al menos una reliquia en su inventario.
     *
     * @return {@code true} si posee una o más reliquias; {@code false} si su inventario está vacío.
     */
    public boolean tieneReliquias() {
        return cantidadReliquias > 0;
    }

    /**
     * Añade una nueva reliquia al inventario del jugador e incrementa el contador de reliquias cargadas.
     *
     * @param reliquia La instancia de {@link Reliquia} recolectada por el jugador.
     */
    public void agregarReliquia(Reliquia reliquia) {
        inventarioReliquias[cantidadReliquias] = reliquia;
        cantidadReliquias++;
    }

    // entrega todas las reliquias y devuelve los puntos ganados
    /**
     * Descarga todas las reliquias presentes en el inventario del jugador, suma sus valores al puntaje total,
     * actualiza el contador de reliquias entregadas y vacía el inventario.
     *
     * @return La cantidad total de puntos obtenidos por las reliquias entregadas en esta operación.
     */
    public int entregarReliquia() {
        int ganados = 0;
        for (int i = 0; i < cantidadReliquias; i++) {
            ganados = ganados + inventarioReliquias[i].getPuntos();
            inventarioReliquias[i] = null;
        }
        reliquiasEntregadas = reliquiasEntregadas + cantidadReliquias;
        cantidadReliquias = 0;
        puntos = puntos + ganados;
        return ganados;
    }

    /**
     * Obtiene el nombre de usuario del jugador.
     *
     * @return El nombre del jugador.
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Obtiene el índice de la fila actual donde se ubica el jugador.
     *
     * @return La coordenada vertical actual en el tablero.
     */
    public int getFilaActual() {
        return filaActual;
    }

    /**
     * Obtiene el índice de la columna actual donde se ubica el jugador.
     *
     * @return La coordenada horizontal actual en el tablero.
     */
    public int getColumnaActual() {
        return columnaActual;
    }

    /**
     * Obtiene la cantidad de reliquias que el jugador lleva actualmente en su inventario.
     *
     * @return El número de reliquias cargadas.
     */
    public int getCantidadReliquias() {
        return cantidadReliquias;
    }

    /**
     * Obtiene el arreglo interno que representa el inventario de reliquias del jugador.
     *
     * @return Un arreglo de {@link Reliquia} con capacidad máxima definida por {@link #CAPACIDAD_RELIQUIAS}.
     */
    public Reliquia[] getInventarioReliquias() {
        return inventarioReliquias;
    }

    /**
     * Obtiene el puntaje total acumulado por el jugador hasta el momento.
     *
     * @return Los puntos acumulados.
     */
    public int getPuntos() {
        return puntos;
    }
    
    /**
     * Obtiene la cantidad total de reliquias que el jugador ha entregado a lo largo de la partida.
     *
     * @return El total de reliquias entregadas.
     */
    public int getReliquiasEntregadas() {
        return reliquiasEntregadas;
    }
}

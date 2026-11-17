package SistemaTurnos;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una instancia inmutable del estado actual del sistema de turnos en un momento dado de la partida
 * Almace la fase del juego, el numero de ronda, el indice del jugador activo,
 * las acciones disponibles y el orden de participacion de los jugadores.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class EstadoTurnos {

    /**
     * Cadena que describe el estado general en el que se encuentra el sistema de turnos
     */
    private String estado;
    /**
     * Número de la ronda actual
     */
    private int ronda;
    /**
     * índice dentro de {@link #ordenJugadores} correspondiente al jugador que tiene su turno activo
     */
    private int indiceJugadorActual;
    /**
     * Cantidad de puntos de acción que le quedan disponibles al jugador actual
     */
    private int accionesRestantes;
    /**
     * Lista ordenada con los nombres de los jugadores
     */
    private List<String> ordenJugadores;
    
    /**
     * Construye una nueva instancia de {@code EstadoTurnos} realizando una copia defensiva
     * de la lista del orden de jugadores.
     *
     * @param estado              Descripción textual del estado actual del sistema de turnos.
     * @param ronda               Número de la ronda en curso.
     * @param indiceJugadorActual Posición en la lista del jugador que tiene el turno activo.
     * @param accionesRestantes   Cantidad de acciones disponibles restantes en el turno.
     * @param ordenJugadores      Lista con el orden de participación de los jugadores.
     */
    public EstadoTurnos(String estado, int ronda, int indiceJugadorActual,
                        int accionesRestantes, List<String> ordenJugadores) {
        this.estado = estado;
        this.ronda = ronda;
        this.indiceJugadorActual = indiceJugadorActual;
        this.accionesRestantes = accionesRestantes;
        this.ordenJugadores = new ArrayList<>(ordenJugadores);
    }

    /**
     * Obtiene la descripción del estado actual del sistema de turnos.
     *
     * @return El estado en formato texto.
     */
    public String obtenerEstado() {
        return estado;
    }
    /**
     * Obtiene el número de la ronda actual.
     *
     * @return Un entero con el número de ronda.
     */
    public int obtenerRonda() {
        return ronda;
    }
    /**
     * Obtiene el índice del jugador que tiene el turno activo.
     *
     * @return La posición del jugador actual dentro de la lista de orden.
     */
    public int obtenerIndiceJugadorActual() {
        return indiceJugadorActual;
    }
    /**
     * Obtiene la cantidad de puntos de acción restantes para el turno en curso.
     *
     * @return Un entero con las acciones disponibles.
     */
    public int obtenerAccionesRestantes() {
        return accionesRestantes;
    }
    
    /**
     * Devuelve una copia de la lista que define el orden de turno de los jugadores.
     *
     * @return Una nueva instancia de {@link List} con los identificadores de los jugadores en orden.
     */
    public List<String> obtenerOrdenJugadores() {
        return new ArrayList<>(ordenJugadores);
    }
}

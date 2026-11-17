package SistemaTurnos;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinador principal del flujo de rondas, turnos y puntos de acción de la partida.
 * Controla la alternancia entre los dos jugadores a lo largo de un máximo de {@value #RONDAS_TOTALES} rondas,
 * valida el consumo de acciones mediante {@link Turno} y permite exportar o restaurar instantáneas
 * del progreso a través de {@link EstadoTurnos}.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class GestorTurnos {

    /**
     * Cantidad máxima de rondas que dura una partida completa.
     */
    public static final int RONDAS_TOTALES = 8;
    
    /**
     * Cantidad de puntos de acción asignados a cada jugador al iniciar su turno.
     */
    public static final int ACCIONES_POR_TURNO = 3;

    /**
     * Constante de texto que identifica una partida que aún se encuentra en desarrollo.
     */
    public static final String EstadoEnCurso = "EN_CURSO";

    /**
     * Constante de texto que identifica una partida que ya ha concluido todas sus rondas.
     */
    public static final String EstadoFinal = "FINALIZADA";

    /**
     * Lista ordenada con los identificadores de los jugadores que participan en la partida.
     */
    private List<String> ordenJugadores;

    /**
     * Índice dentro de {@link #ordenJugadores} correspondiente al jugador que tiene el turno activo.
     */
    private int indiceJugadorActual;

    /**
     * Número de la ronda en curso (comienza en 1 hasta llegar a {@link #RONDAS_TOTALES}).
     */
    private int ronda;

    /**
     * Objeto {@link Turno} activo que administra las acciones restantes del jugador en curso.
     */
    private Turno turnoActual;

    /**
     * Indica si la partida ya alcanzó el límite de rondas y ha finalizado ({@code true}) o sigue activa ({@code false}).
     */
    private boolean partidaFinalizada;

    /**
     * Construye una nueva instancia de {@code GestorTurnos} con sus valores iniciales por defecto,
     * lista para comenzar una partida mediante {@link #iniciarPartida(List)}.
     */
    public GestorTurnos() {
        this.ordenJugadores = new ArrayList<>();
        this.indiceJugadorActual = 0;
        this.ronda = 1;
        this.turnoActual = null;
        this.partidaFinalizada = false;
    }

    /**
     * Inicializa el estado de una nueva partida estableciendo el orden de participación de los dos jugadores,
     * reiniciando el contador de rondas a 1 y creando el primer {@link Turno} activo.
     *
     * @param ordenDesdeApi Lista con exactamente dos nombres o identificadores de jugadores sorteados por el servidor.
     * @throws IllegalArgumentException Si la lista es {@code null} o no contiene exactamente 2 jugadores.
     */
    public void iniciarPartida(List<String> ordenDesdeApi) {
        if (ordenDesdeApi == null || ordenDesdeApi.size() != 2) {
            throw new IllegalArgumentException("La partida necesita exactamente dos jugadores");
        }

        ordenJugadores = new ArrayList<>(ordenDesdeApi);
        indiceJugadorActual = 0;
        ronda = 1;
        partidaFinalizada = false;
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), ACCIONES_POR_TURNO);
    }
    
    /**
     * Restaura el estado interno del gestor a partir de una instantánea previamente guardada en un {@link EstadoTurnos}.
     * Si el estado restaurado corresponde a una partida finalizada, cierra automáticamente el turno reconstruido.
     *
     * @param estadoGuardado Objeto {@link EstadoTurnos} con los datos de ronda, jugador y acciones a restablecer.
     */
    public void restaurar(EstadoTurnos estadoGuardado) {
        ordenJugadores = estadoGuardado.obtenerOrdenJugadores();
        indiceJugadorActual = estadoGuardado.obtenerIndiceJugadorActual();
        ronda = estadoGuardado.obtenerRonda();
        partidaFinalizada = estadoGuardado.obtenerEstado().equals(EstadoFinal);
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual),
                estadoGuardado.obtenerAccionesRestantes());

        if (partidaFinalizada) {
            turnoActual.cerrar();
        }
    }

    /**
     * Comprueba si el jugador actual dispone de puntos de acción suficientes para realizar una operación
     * sin llegar a descontarlos todavía.
     *
     * @param costo Cantidad de acciones requeridas.
     * @return {@code true} si la partida sigue en curso y el turno actual puede cubrir el costo;
     *         {@code false} en caso contrario.
     */
    public boolean puedeGastarAcciones(int costo) {
        if (partidaFinalizada) {
            return false;
        }
        return turnoActual.puedeGastar(costo);
    }

    /**
     * Descuenta una cantidad de puntos de acción del turno en curso.
     * Si tras el gasto el jugador se queda sin acciones disponibles, avanza automáticamente al siguiente turno
     * invocando {@link #terminarTurno()}.
     *
     * @param costo Cantidad de acciones a consumir.
     * @return {@code true} si las acciones se descontaron con éxito; {@code false} si la partida terminó
     *         o si el jugador no tenía suficientes acciones.
     */
    public boolean gastarAcciones(int costo) {
        if (partidaFinalizada) {
            return false;
        }

        boolean sePudoGastar = turnoActual.gastar(costo);

        if (!sePudoGastar) {
            return false;
        }

        if (turnoActual.estaAgotado()) {
            terminarTurno();
        }

        return true;
    }

    /**
     * Cierra el turno del jugador actual y cede el control al siguiente participante.
     * Si todos los jugadores ya jugaron en la ronda actual, incrementa el número de ronda;
     * si se supera {@link #RONDAS_TOTALES}, marca la partida como finalizada.
     */
    public void terminarTurno() {
        if (partidaFinalizada) {
            return;
        }

        turnoActual.cerrar();

        int siguienteIndice = indiceJugadorActual + 1;
        int siguienteRonda = ronda;

        if (siguienteIndice >= ordenJugadores.size()) {
            siguienteIndice = 0;
            siguienteRonda = siguienteRonda + 1;
        }

        if (siguienteRonda > RONDAS_TOTALES) {
            partidaFinalizada = true;
            return;
        }

        indiceJugadorActual = siguienteIndice;
        ronda = siguienteRonda;
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), ACCIONES_POR_TURNO);
    }

    /**
     * Genera y devuelve una instantánea actual del estado del sistema de turnos.
     *
     * @return Una nueva instancia de {@link EstadoTurnos} con la fase, ronda, jugador activo y acciones restantes.
     */
    public EstadoTurnos obtenerEstado() {
        String textoEstado = EstadoEnCurso;
        if (partidaFinalizada) {
            textoEstado = EstadoFinal;
        }

        return new EstadoTurnos(textoEstado, ronda, indiceJugadorActual,
                obtenerAccionesRestantes(), ordenJugadores);
    }

    /**
     * Obtiene el identificador o nombre de usuario del jugador que posee el turno actual.
     *
     * @return El nombre del jugador activo.
     */
    public String obtenerJugadorActual() {
        return ordenJugadores.get(indiceJugadorActual);
    }

    /**
     * Obtiene el número de la ronda en curso.
     *
     * @return Un entero entre {@code 1} y {@link #RONDAS_TOTALES}.
     */
    public int obtenerRonda() {
        return ronda;
    }

    /**
     * Obtiene la cantidad de puntos de acción que le quedan al jugador en el turno actual.
     *
     * @return Las acciones restantes, o {@code 0} si la partida ya finalizó.
     */
    public int obtenerAccionesRestantes() {
        if (partidaFinalizada) {
            return 0;
        }
        return turnoActual.obtenerAccionesRestantes();
    }

    /**
     * Verifica si la partida ha concluido todas sus rondas.
     *
     * @return {@code true} si la partida está finalizada; {@code false} si aún sigue en curso.
     */
    public boolean estaFinalizada() {
        return partidaFinalizada;
    }
}

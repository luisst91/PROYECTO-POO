package SistemaTurnos;

import java.util.ArrayList;
import java.util.List;

public class GestorTurnos {

    /**
     * Cantidad de rondas que dura la partida. No tiene un valor fijo: se coloca con
     * {@link #setRondasTotales(int)} con el valor que entrega la API.
     */
    private static int rondasTotales;
    /**
     * Puntos de acción con los que empieza cada turno. No tiene un valor fijo: se coloca con
     * {@link #setAccionesPorTurno(int)} con el valor que entrega la API.
     */
    private static int accionesPorTurno;
    public static final String estadoEnCurso = "EN_CURSO";
    public static final String estadoFinal = "FINALIZADA";

    private List<String> ordenJugadores;
    private int indiceJugadorActual;
    private int ronda;
    private Turno turnoActual;
    private boolean partidaFinalizada;

    /**
     * Devuelve la cantidad de rondas que dura la partida.
     *
     * @return El número de rondas configurado
     */
    public static int getRondasTotales() {
        return rondasTotales;
    }

    /**
     * Coloca la cantidad de rondas de la partida. Se debe llamar antes de iniciar la partida.
     *
     * @param nuevasRondas Nuevo número de rondas que entrega la API
     */
    public static void setRondasTotales(int nuevasRondas) {
        rondasTotales = nuevasRondas;
    }

    /**
     * Devuelve los puntos de acción que recibe cada jugador al empezar su turno.
     *
     * @return Los puntos de acción por turno configurados
     */
    public static int getAccionesPorTurno() {
        return accionesPorTurno;
    }

    /**
     * Coloca los puntos de acción por turno. Se debe llamar antes de iniciar la partida.
     *
     * @param nuevasAcciones Nuevos puntos de acción que entrega la API
     */
    public static void setAccionesPorTurno(int nuevasAcciones) {
        accionesPorTurno = nuevasAcciones;
    }

    public GestorTurnos() {
        this.ordenJugadores = new ArrayList<>();
        this.indiceJugadorActual = 0;
        this.ronda = 1;
        this.turnoActual = null;
        this.partidaFinalizada = false;
    }

    public void iniciarPartida(List<String> ordenDesdeApi) {
        if (ordenDesdeApi == null || ordenDesdeApi.size() != 2) {
            throw new IllegalArgumentException("La partida necesita exactamente dos jugadores");
        }

        ordenJugadores = new ArrayList<>(ordenDesdeApi);
        indiceJugadorActual = 0;
        ronda = 1;
        partidaFinalizada = false;
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), accionesPorTurno);
    }

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

    public boolean puedeGastarAcciones(int costo) {
        if (partidaFinalizada) {
            return false;
        }
        return turnoActual.puedeGastar(costo);
    }

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

        if (siguienteRonda > rondasTotales) {
            partidaFinalizada = true;
            return;
        }

        indiceJugadorActual = siguienteIndice;
        ronda = siguienteRonda;
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), accionesPorTurno);
    }

    public EstadoTurnos obtenerEstado() {
        String textoEstado = EstadoEnCurso;
        if (partidaFinalizada) {
            textoEstado = EstadoFinal;
        }

        return new EstadoTurnos(textoEstado, ronda, indiceJugadorActual,
                obtenerAccionesRestantes(), ordenJugadores);
    }

    public String obtenerJugadorActual() {
        return ordenJugadores.get(indiceJugadorActual);
    }

    public int obtenerRonda() {
        return ronda;
    }

    public int obtenerAccionesRestantes() {
        if (partidaFinalizada) {
            return 0;
        }
        return turnoActual.obtenerAccionesRestantes();
    }

    public boolean estaFinalizada() {
        return partidaFinalizada;
    }
}

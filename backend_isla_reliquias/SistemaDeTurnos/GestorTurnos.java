package SistemaDeTurnos;

import java.util.ArrayList;
import java.util.List;

public class GestorTurnos {

    public static final int RondasTotales = 8;
    public static final int AccionesPorTurno = 3;
    public static final String EstadoEnCurso = "EN_CURSO";
    public static final String EstadoFinal = "FINALIZADA";

    private List<String> ordenJugadores;
    private int indiceJugadorActual;
    private int ronda;
    private Turno turnoActual;
    private boolean partidaFinalizada;

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
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), AccionesPorTurno);
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

        if (siguienteRonda > RondasTotales) {
            partidaFinalizada = true;
            return;
        }

        indiceJugadorActual = siguienteIndice;
        ronda = siguienteRonda;
        turnoActual = new Turno(ordenJugadores.get(indiceJugadorActual), AccionesPorTurno);
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

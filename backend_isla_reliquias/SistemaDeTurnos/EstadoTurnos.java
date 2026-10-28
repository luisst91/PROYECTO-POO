package SistemaDeTurnos;

import java.util.ArrayList;
import java.util.List;

public class EstadoTurnos {

    private String estado;
    private int ronda;
    private int indiceJugadorActual;
    private int accionesRestantes;
    private List<String> ordenJugadores;

    public EstadoTurnos(String estado, int ronda, int indiceJugadorActual,
                        int accionesRestantes, List<String> ordenJugadores) {
        this.estado = estado;
        this.ronda = ronda;
        this.indiceJugadorActual = indiceJugadorActual;
        this.accionesRestantes = accionesRestantes;
        this.ordenJugadores = new ArrayList<>(ordenJugadores);
    }

    public String obtenerEstado() {
        return estado;
    }

    public int obtenerRonda() {
        return ronda;
    }

    public int obtenerIndiceJugadorActual() {
        return indiceJugadorActual;
    }

    public int obtenerAccionesRestantes() {
        return accionesRestantes;
    }

    public List<String> obtenerOrdenJugadores() {
        return new ArrayList<>(ordenJugadores);
    }
}

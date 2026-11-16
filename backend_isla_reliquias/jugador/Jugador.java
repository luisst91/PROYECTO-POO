package jugador;

import casillas.Casilla;
import objetos.Reliquia;

public class Jugador {
    public static final int CAPACIDAD_RELIQUIAS = 2;

    private String nombreUsuario;
    private int filaActual;
    private int columnaActual;
    private Reliquia[] inventarioReliquias = new Reliquia[CAPACIDAD_RELIQUIAS];
    private int cantidadReliquias = 0;
    private int puntos = 0;
    private int reliquiasEntregadas = 0;

    public Jugador(String nombreUsuario, int filaActual, int columnaActual) {
        this.nombreUsuario = nombreUsuario;
        this.filaActual = filaActual;
        this.columnaActual = columnaActual;
    }

    // true si la casilla esta justo arriba, abajo, a la izquierda o a la derecha
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

    public void moverJugador(Casilla destino) {
        filaActual = destino.getFila();
        columnaActual = destino.getColumna();
    }

    public boolean tieneEspacio() {
        return cantidadReliquias < CAPACIDAD_RELIQUIAS;
    }

    public boolean tieneReliquias() {
        return cantidadReliquias > 0;
    }

    public void agregarReliquia(Reliquia reliquia) {
        inventarioReliquias[cantidadReliquias] = reliquia;
        cantidadReliquias++;
    }

    // entrega todas las reliquias y devuelve los puntos ganados
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

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getFilaActual() {
        return filaActual;
    }

    public int getColumnaActual() {
        return columnaActual;
    }

    public int getCantidadReliquias() {
        return cantidadReliquias;
    }

    public Reliquia[] getInventarioReliquias() {
        return inventarioReliquias;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getReliquiasEntregadas() {
        return reliquiasEntregadas;
    }
}

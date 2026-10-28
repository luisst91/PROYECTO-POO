package jugador;

import objetos.Pista;
import objetos.Reliquia;

public class Jugador {
    public static final int PUNTOS_POR_TURNO = 3;
    private String nombreUsuario;
    private int puntosRestantes;
    private int filaActual;
    private int columnaActual;
    private Reliquia[] inventarioReliquias= new Reliquia[2];
    private Pista[] inventarioPistas= new Pista[2];
    private boolean estadoTurno;

    public Jugador(String nombreUsuario, int puntosRestantes, int filaActual, int columnaActual, Reliquia[] inventarioReliquias, boolean estadoTurno) {
        this.nombreUsuario = nombreUsuario;
        this.puntosRestantes = puntosRestantes;
        this.filaActual = filaActual;
        this.columnaActual = columnaActual;
        this.inventarioReliquias = inventarioReliquias;
        this.estadoTurno = estadoTurno;
    }
    public moverJugador(Casilla destino){

    }

    public void entregarReliquia(){

    }
}

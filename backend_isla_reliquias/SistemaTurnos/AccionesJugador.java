package SistemaTurnos;

import casillas.Casilla;
import casillas.Tablero;
import jugador.Jugador;

public class AccionesJugador {
    private Tablero tablero;
    private GestorTurnos gestorTurnos;

    // Verificar Constructores

    public void procesarMovimiento(Jugador jugadorActual, int filaNueva, int columnaNueva){

        Casilla destino = tablero.obtenerCasilla(filaNueva, columnaNueva);
        if (destino == null){
            System.out.println("ERROR. LA COORDENADA INGRESADA NO ES VALIDA: FUERA DE LOS LIMITES");
        }
        else{
            if (gestorTurnos.gastarAcciones(destino.obtenerCostoMovimiento())){
                jugadorActual.actualizarCoordenadas(destino);
                System.out.println("MOVIMIENTO EXITOSO A: " + destino.getNombreCasilla());
            }
            else{
                System.out.println("NO TIENES ACCIONES SUFICIENTES PARA REALIZAR EL MOVIMIENTO");
            }
        }
    }
}

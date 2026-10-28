package SistemaDeTurnos;

public class Turno {

    private String jugadorId;
    private int accionesRestantes;
    private boolean finalizar;

    public Turno(String jugadorId, int accionesRestantes) {
        this.jugadorId = jugadorId;
        this.accionesRestantes = accionesRestantes;
        this.finalizar = false;
    }

    public boolean puedeGastar(int costo) {
        if (finalizar || costo < 0) {
            return false;
        }
        return costo <= accionesRestantes;
    }

    public boolean gastar(int costo) {
        if (!puedeGastar(costo)) {
            return false;
        }
        accionesRestantes = accionesRestantes - costo;
        return true;
    }

    public boolean estaAgotado() {
        return accionesRestantes == 0;
    }

    public void cerrar() {
        finalizar = true;
    }

    public String obtenerJugadorId() {
        return jugadorId;
    }

    public int obtenerAccionesRestantes() {
        return accionesRestantes;
    }

    public boolean estaCerrado() {
        return finalizar;
    }
}
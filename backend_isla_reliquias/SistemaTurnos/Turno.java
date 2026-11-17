package SistemaTurnos;

/**
 * Representa el turno individual de un jugador dentro de una ronda de juego.
 * Administra la cantidad de puntos de acción disponibles, valida y descuenta los costos
 * de las acciones realizadas y controla si el turno ha finalizado o se ha agotado.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Turno {
    /**
     * Identificador o nombre de usuario del jugador al que pertenece este turno.
     */
    private String jugadorId;

    /**
     * Cantidad de puntos de acción disponibles que le quedan al jugador en este turno.
     */
    
    private int accionesRestantes;

    /**
     * Bandera que indica si el turno ha sido dado por terminado ({@code true}) o sigue activo ({@code false}).
     */
    private boolean finalizar;

    /**
     * Construye una nueva instancia de un Turno para un jugador con una cantidad inicial de acciones.
     *
     * @param jugadorId         Identificador del jugador propietario del turno.
     * @param accionesRestantes Cantidad inicial de acciones asignadas para este turno.
     */
    public Turno(String jugadorId, int accionesRestantes) {
        this.jugadorId = jugadorId;
        this.accionesRestantes = accionesRestantes;
        this.finalizar = false;
    }

    /**
     * Verifica si el jugador tiene suficientes acciones para cubrir un costo determinado,
     * comprobando así que el turno no haya terminado y que el costo no sea negativo.
     *
     * @param costo Cantidad de puntos de acción requeridos para realizar la operación.
     * @return {@code true} si el turno sigue activo, el costo es válido y alcanza con las acciones actuales;
     *         {@code false} en caso contrario.
     */
    public boolean puedeGastar(int costo) {
        if (finalizar || costo < 0) {
            return false;
        }
        return costo <= accionesRestantes;
    }
    /**
     * Descuenta una cantidad de puntos de acción del saldo disponible si la operación es válida.
     *
     * @param costo Cantidad de puntos de acción a consumir.
     * @return {@code true} si las acciones fueron descontadas exitosamente; {@code false} si no era posible gastar esa cantidad.
     */
    public boolean gastar(int costo) {
        if (!puedeGastar(costo)) {
            return false;
        }
        accionesRestantes = accionesRestantes - costo;
        return true;
    }
    /**
     * Comprueba si el jugador ya ha gastado la totalidad de sus acciones en este turno.
     *
     * @return {@code true} si {@link #accionesRestantes} es igual a {@code 0}; {@code false} si aún le quedan acciones.
     */
    public boolean estaAgotado() {
        return accionesRestantes == 0;
    }
    /**
     * Marca el turno como finalizado para impedir que se sigan realizando acciones en él.
     */
    public void cerrar() {
        finalizar = true;
    }
    
    /**
     * Obtiene el identificador del jugador al que corresponde este turno.
     *
     * @return El identificador del jugador en formato texto.
     */
    public String obtenerJugadorId() {
        return jugadorId;
    }

    /**
     * Obtiene la cantidad de puntos de acción que aún tiene disponibles el jugador en este turno.
     *
     * @return Un entero con las acciones restantes.
     */
    public int obtenerAccionesRestantes() {
        return accionesRestantes;
    }
    /**
     * Verifica si el turno ya fue marcado como finalizado.
     *
     * @return {@code true} si el turno está terminado; {@code false} si continúa en curso.
     */
    public boolean estaCerrado() {
        return finalizar;
    }
}

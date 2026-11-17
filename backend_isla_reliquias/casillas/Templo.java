package casillas;

import objetos.Pista;
import objetos.Reliquia;

/**
 * Casilla de templo normal (símbolo T). Guarda una {@link Reliquia} y, en algunos templos, una {@link Pista}
 * con un fragmento de la contraseña del cofre. El contenido se descubre al explorar el templo, y una vez
 * explorado no vuelve a dar su reliquia.
 *
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class Templo extends Casilla {

    /**
     * Reliquia que se encuentra dentro del templo
     */
    private Reliquia reliquia;
    /**
     * Pista numerada que se revela al explorar, o {@code null} si el templo no tiene ninguna
     */
    private Pista pista;
    /**
     * Indica si el templo ya fue explorado por algún jugador
     */
    private boolean explorado = false;

    /**
     * Construye un templo con su contenido y su estado de exploración.
     * Al crear el tablero se usa con {@code null} en reliquia y pista y {@code false} en explorado,
     * porque el contenido real se pide a la API cuando un jugador explora.
     *
     * @param nombreCasilla Nombre que se muestra del terreno ("Templo")
     * @param simbolo Letra que lo representa en el tablero ('T')
     * @param fila Fila donde se ubica la casilla
     * @param columna Columna donde se ubica la casilla
     * @param reliquia Reliquia que guarda el templo
     * @param pista Pista que guarda el templo, o {@code null} si no tiene
     * @param explorado {@code true} si el templo ya fue explorado
     */
    public Templo(String nombreCasilla, char simbolo, int fila, int columna, Reliquia reliquia, Pista pista, boolean explorado) {
        super(nombreCasilla, simbolo, fila, columna);
        this.reliquia = reliquia;
        this.pista = pista;
        this.explorado = explorado;
    }

    /**
     * Devuelve la reliquia del templo.
     *
     * @return La {@link Reliquia} guardada en el templo
     */
    public Reliquia getReliquia() {
        return reliquia;
    }

    /**
     * Cambia la reliquia del templo.
     *
     * @param reliquia La nueva {@link Reliquia} del templo
     */
    public void setReliquia(Reliquia reliquia) {
        this.reliquia = reliquia;
    }

    /**
     * Devuelve la pista del templo.
     *
     * @return La {@link Pista} guardada en el templo, o {@code null} si no tiene
     */
    public Pista getPista() {
        return pista;
    }

    /**
     * Cambia la pista del templo. Se usa al explorar, cuando la API revela el fragmento.
     *
     * @param pista La nueva {@link Pista} del templo
     */
    public void setPista(Pista pista) {
        this.pista = pista;
    }

    /**
     * Indica si el templo ya fue explorado.
     *
     * @return {@code true} si ya fue explorado, {@code false} si todavía no
     */
    public boolean isExplorado() {
        return explorado;
    }

    /**
     * Marca el templo como explorado o no explorado.
     *
     * @param explorado {@code true} para marcarlo como explorado
     */
    public void setExplorado(boolean explorado) {
        this.explorado = explorado;
    }

}

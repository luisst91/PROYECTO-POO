package objetos;

/**
 * Representa una pista obtenida al explorar un templo dentro del juego.
 * Contiene un número identificativo y el texto descriptivo que ayuda a los jugadores
 * a deducir la clave secreta para abrir el cofre.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Pista {
    /**
     * Número identificativo de la pista
     */
    private int numeroPista;
    /**
     * Texto revelador que contiene la pista
     */
    private String contenido;


    /**
     * Construye una nueva instancia de una Pista con su número identificativo y su contenido
     * 
     * 
     * @param numeroPista El número que identifica a la pista
     * @param contenido El texto que conforma la pista
     */
    public Pista(int numeroPista, String contenido) {
        this.numeroPista = numeroPista;
        this.contenido = contenido;
    }

    /**
     * Obtiene el número identificativo de la pista
     * 
     * @return El entero que representa el número de la pista
     */
    public int getNumeroPista() {
        return numeroPista;
    }

    /**
     * Obtiene el contenido textual de la pista
     * 
     * @return Una cadena de texto con la información revelada por la pista
     */
    public String getContenido() {
        return contenido;
    }

}

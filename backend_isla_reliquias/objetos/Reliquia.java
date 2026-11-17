package objetos;

/**
 * Representa una reliauiq que puede ser recolectda al explorar templos
 * y posteriormente entregada en el campamento para obtener puntos
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class Reliquia {


    /**
     * Identificador único de la reliquia
     */
    private String id;
    /**
     * Nombre de la reliquia
     */
    private String nombre;
    /**
     * Cantidad de puntos que otorga la reliauiq al ser entregada por un jugador
     */
    private int puntos;


    /**
     * Construye una nueva instancia de una Reliquia con su identificador, nombre y valor en puntos
     * 
     * @param id El identificador único de la reliquia
     * @param nombre El nombre de la reliquia
     * @param puntos El valor en puntos que aporta la reliquia
     */
    public Reliquia(String id, String nombre, int puntos) {
        this.id = id;
        this.nombre = nombre;
        this.puntos = puntos;
    }

    /**
     * Obtiene el identificador único de la reliquia
     * 
     * @return El identificador de la reliauiq en forma de texto
     */
    public String getId() {
        return id;
    }
    /**
     * Obtiene el nombre de la reliquia
     * 
     * @return El nombre de la reliquia
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el valor en puntos asociado a la reliquia
     * 
     * @return Un entero con la cantidad de puntos que vale la reliquia
     */
    public int getPuntos() {
        return puntos;
    }

}

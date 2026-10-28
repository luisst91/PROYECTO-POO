package objetos;

public class Reliquia {

    private String id;
    private String nombre;
    private int puntos;

    public Reliquia(String id, String nombre, int puntos) {
        this.id = id;
        this.nombre = nombre;
        this.puntos = puntos;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntos() {
        return puntos;
    }

}

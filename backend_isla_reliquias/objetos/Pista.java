package objetos;

public class Pista {
    private int numeroPista;
    private String contenido;

    public Pista(int numeroPista, String contenido) {
        this.numeroPista = numeroPista;
        this.contenido = contenido;
    }

    public int getNumeroPista() {
        return numeroPista;
    }

    public String getContenido() {
        return contenido;
    }

}

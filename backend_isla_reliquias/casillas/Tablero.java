package casillas;

public class Tablero {

    public static final int FILAS = 5;
    public static final int COLUMNAS = 5;

    private Casilla[][] casillas = new Casilla[FILAS][COLUMNAS];

    // recibe la matriz de letras que manda la API: C, X, T, P, S
    public Tablero(char[][] simbolos) {
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                casillas[f][c] = crearCasilla(simbolos[f][c], f, c);
            }
        }
    }

    private Casilla crearCasilla(char simbolo, int fila, int columna) {
        if (simbolo == 'C') {
            return new Campamento("Campamento", simbolo, fila, columna);
        }
        if (simbolo == 'X') {
            return new Cofre("Cofre", simbolo, fila, columna);
        }
        if (simbolo == 'T') {
            return new Templo("Templo", simbolo, fila, columna, null, null, false);
        }
        if (simbolo == 'P') {
            return new Pantano("Pantano", simbolo, fila, columna);
        }
        return new Sendero("Sendero", simbolo, fila, columna);
    }

    // devuelve null si la coordenada esta fuera del tablero
    public Casilla obtenerCasilla(int fila, int columna) {
        if (fila < 0 || fila >= FILAS || columna < 0 || columna >= COLUMNAS) {
            return null;
        }
        return casillas[fila][columna];
    }

    // recibe algo como "B2" (letra = columna, numero = fila). Devuelve null si no es valida
    public Casilla obtenerCasilla(String coordenada) {
        if (coordenada == null || coordenada.length() != 2) {
            return null;
        }
        int columna = coordenada.charAt(0) - 'A';
        int fila = coordenada.charAt(1) - '1';
        return obtenerCasilla(fila, columna);
    }

    public static String convertirACoordenada(int fila, int columna) {
        return "" + (char) ('A' + columna) + (fila + 1);
    }
}

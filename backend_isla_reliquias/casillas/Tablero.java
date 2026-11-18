package casillas;

/**
 * Representa el mapa de la partida: una cuadrícula de casillas (el tamaño lo define la API) donde se mueven los jugadores.
 * Se construye a partir de la matriz de letras enviada por la API (C, X, T, P, S) y convierte
 * cada letra en el objeto {@link Casilla} que le corresponde (Campamento, Cofre, Templo, Pantano o Sendero).
 * Las columnas se nombran con letras (A-E) y las filas con números (1-5), por ejemplo "B2".
 *
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class Tablero {

    /**
     * Cantidad de filas del tablero. No tiene un valor fijo: se coloca con {@link #setFilas(int)}
     * con el valor que entrega la API.
     */
    private static int filas;
    /**
     * Cantidad de columnas del tablero. No tiene un valor fijo: se coloca con {@link #setColumnas(int)}
     * con el valor que entrega la API.
     */
    private static int columnas;

    /**
     * Devuelve la cantidad de filas que tiene el tablero.
     *
     * @return El número de filas configurado
     */
    public static int getFilas() {
        return filas;
    }

    /**
     * Coloca la cantidad de filas del tablero. Se debe llamar antes de crear un {@link Tablero}.
     *
     * @param nuevasFilas Nuevo número de filas que entrega la API
     */
    public static void setFilas(int nuevasFilas) {
        filas = nuevasFilas;
    }

    /**
     * Devuelve la cantidad de columnas que tiene el tablero.
     *
     * @return El número de columnas configurado
     */
    public static int getColumnas() {
        return columnas;
    }

    /**
     * Coloca la cantidad de columnas del tablero. Se debe llamar antes de crear un {@link Tablero}.
     *
     * @param nuevasColumnas Nuevo número de columnas que entrega la API
     */
    public static void setColumnas(int nuevasColumnas) {
        columnas = nuevasColumnas;
    }

    /**
     * Matriz que guarda cada una de las casillas del tablero, indexada como [fila][columna]
     */
    private Casilla[][] casillas = new Casilla[filas][columnas];

    /**
     * Construye el tablero recorriendo la matriz de letras recibida de la API y creando
     * una casilla por cada símbolo mediante {@link #crearCasilla(char, int, int)}.
     *
     * @param simbolos Matriz de filas X columnas con los símbolos de cada casilla: C, X, T, P o S
     */
    public Tablero(char[][] simbolos) {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                casillas[f][c] = crearCasilla(simbolos[f][c], f, c);
            }
        }
    }

    /**
     * Crea el tipo de casilla que corresponde a un símbolo: C para el {@link Campamento}, X para el {@link Cofre},
     * T para un {@link Templo} (sin reliquia ni pista hasta que se explore), P para un {@link Pantano}
     * y cualquier otro símbolo (S) para un {@link Sendero}.
     *
     * @param simbolo Letra que identifica el tipo de terreno
     * @param fila Fila donde se ubicará la casilla
     * @param columna Columna donde se ubicará la casilla
     * @return La casilla creada, con el tipo que corresponde al símbolo
     */
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

    /**
     * Busca la casilla que se encuentra en una fila y columna específicas del tablero.
     *
     * @param fila Número de fila (empieza en 0)
     * @param columna Número de columna (empieza en 0)
     * @return La {@link Casilla} de esa posición, o {@code null} si la coordenada está fuera de los límites del tablero
     */
    public Casilla obtenerCasilla(int fila, int columna) {
        if (fila < 0 || fila >= filas || columna < 0 || columna >= columnas) {
            return null;
        }
        return casillas[fila][columna];
    }

    /**
     * Busca una casilla a partir de su coordenada escrita en texto, por ejemplo {@code "B2"},
     * donde la letra es la columna (A = 0) y el número es la fila (1 = 0).
     *
     * @param coordenada Texto de dos caracteres con la letra de la columna y el número de la fila
     * @return La {@link Casilla} de esa coordenada, o {@code null} si el texto es nulo, no tiene dos caracteres o está fuera del tablero
     */
    public Casilla obtenerCasilla(String coordenada) {
        if (coordenada == null || coordenada.length() != 2) {
            return null;
        }
        int columna = coordenada.charAt(0) - 'A';
        int fila = coordenada.charAt(1) - '1';
        return obtenerCasilla(fila, columna);
    }

    /**
     * Convierte una posición numérica en su coordenada de texto. Por ejemplo, fila 1 y columna 1 se convierten en {@code "B2"}.
     *
     * @param fila Número de fila (empieza en 0)
     * @param columna Número de columna (empieza en 0)
     * @return La coordenada en texto, con la letra de la columna seguida del número de la fila
     */
    public static String convertirACoordenada(int fila, int columna) {
        return "" + (char) ('A' + columna) + (fila + 1);
    }
}

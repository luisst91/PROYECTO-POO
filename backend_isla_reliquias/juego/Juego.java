package juego;

import SistemaTurnos.AccionesJugador;
import SistemaTurnos.GestorTurnos;
import casillas.Casilla;
import casillas.Pantano;
import casillas.Tablero;
import jugador.Jugador;
import objetos.Reliquia;

import java.util.ArrayList;
import java.util.Scanner;

// Menu principal y ciclo del juego en consola
/**
 * Clase principal encargada de gestionar la interfaz de consola, el menú de inicio
 * y el ciclo principal de turnos de "La Isla de las Reliquias".
 * Coordina la interacción entre los usuarios, el tablero, el gestor de turnos y el cliente API.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */
public class Juego {
    /**
     * Dirección URL bse donde se encuentra el backend del juego
     */
    public static final String URL_API = "http://127.0.0.1:8000";

    /**
     * Punto de entrada principal de la aplicación
     * Inicializa el lector de consola y el cliente HTTP, desplegando el menu principal en un bucle
     * hasta que el usuario decida salir
     * 
     * @param args Argumentos de linea de comandos
     */
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        ClienteApi cliente = new ClienteApi(URL_API);

        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("===== LA ISLA DE LAS RELIQUIAS =====");
            System.out.println("1. Nueva partida");
            System.out.println("0. Salir");
            System.out.print("Elige una opcion: ");
            String opcion = teclado.nextLine().trim();

            if (opcion.equals("1")) {
                nuevaPartida(teclado, cliente);
            } else if (opcion.equals("0")) {
                salir = true;
            } else {
                System.out.println("OPCION NO VALIDA");
            }
        }
        System.out.println("Hasta pronto!");
    }

    /**
     * Solicita y valida por consola el nombre de un jugador, verificando de que no esté vacio
     * ni contenga caracteres que puedan romper la estructura del JSON ({@code "} o {@code \})
     * 
     * 
     * @param teclado Instancia de {@link Scanner} para leer la entrada del usuario
     * @param texto Mensaje descriptivo que se mostrará al pedir el dato
     * @return El nombre validado y sin espacios extremos
     */


    private static String pedirNombre(Scanner teclado, String texto) {
        String nombre = "";
        while (nombre.length() == 0 || nombre.indexOf('"') != -1 || nombre.indexOf('\\') != -1) {
            System.out.print(texto);
            nombre = teclado.nextLine().trim();
            if (nombre.length() == 0 || nombre.indexOf('"') != -1 || nombre.indexOf('\\') != -1) {
                System.out.println("EL NOMBRE NO PUEDE ESTAR VACIO NI LLEVAR COMILLAS NI BARRAS");
            }
        }
        return nombre;
    }
    /**
     * Coloca en los atributos de cada clase los valores que entregó la API. Es el método que INSERTA los valores
     * (el que los saca de la API es {@link ClienteApi#obtenerConfiguracion()}). Cada dato se lee del JSON y se pasa
     * directamente al set de la clase a la que pertenece: tamaño del tablero a {@link Tablero}, rondas y acciones por
     * turno a {@link GestorTurnos}, capacidad del inventario a {@link Jugador}, costo de pantano a {@link Pantano},
     * costo de casilla normal a {@link Casilla} y costos de explorar, entregar y abrir el cofre a {@link AccionesJugador}.
     * Debe llamarse ANTES de crear el tablero, los jugadores y el gestor de turnos.
     *
     * @param json Cadena en formato JSON con la configuración, o {@code null} si la API no respondió
     * @return {@code true} si se colocaron todos los valores; {@code false} si la API no respondió o falta algún dato
     */
    private static boolean aplicarConfiguracion(String json) {
        if (json == null) {
            System.out.println("NO SE PUDO LEER LA CONFIGURACION DE LA API. NO SE PUEDE INICIAR LA PARTIDA");
            return false;
        }
        String[] claves = {"filas", "columnas", "rondasTotales", "accionesPorTurno", "capacidadInventario", "costoMovimiento", "costoPantano", "costoExplorar", "costoEntregar", "costoAbrirCofre"};
        for (int i = 0; i < claves.length; i++) {
            if (ClienteApi.valorDe(json, claves[i]) == null) {
                System.out.println("LA CONFIGURACION DE LA API NO TRAE EL DATO " + claves[i]);
                return false;
            }
        }
        Tablero.setFilas(Integer.parseInt(ClienteApi.valorDe(json, "filas")));
        Tablero.setColumnas(Integer.parseInt(ClienteApi.valorDe(json, "columnas")));
        GestorTurnos.setRondasTotales(Integer.parseInt(ClienteApi.valorDe(json, "rondasTotales")));
        GestorTurnos.setAccionesPorTurno(Integer.parseInt(ClienteApi.valorDe(json, "accionesPorTurno")));
        Jugador.setCapacidadReliquias(Integer.parseInt(ClienteApi.valorDe(json, "capacidadInventario")));
        Casilla.setCostoNormal(Integer.parseInt(ClienteApi.valorDe(json, "costoMovimiento")));
        Pantano.setCostoPantano(Integer.parseInt(ClienteApi.valorDe(json, "costoPantano")));
        AccionesJugador.setCostoExplorar(Integer.parseInt(ClienteApi.valorDe(json, "costoExplorar")));
        AccionesJugador.setCostoEntregar(Integer.parseInt(ClienteApi.valorDe(json, "costoEntregar")));
        AccionesJugador.setCostoAbrirCofre(Integer.parseInt(ClienteApi.valorDe(json, "costoAbrirCofre")));
        return true;
    }

/**
     * Configura y ejecuta una partida completa entre dos jugadores.
     * Crea la sesión en el servidor mediante {@link ClienteApi}, inicializa el {@link Tablero},
     * posiciona a ambos {@link Jugador} en el campamento y controla el bucle de acciones por turno
     * hasta que finalicen las rondas o se abandone la partida.
     *
     * @param teclado Instancia de {@link Scanner} para leer las decisiones de los jugadores.
     * @param cliente Instancia de {@link ClienteApi} para comunicarse con el servidor.
     */

    private static void nuevaPartida(Scanner teclado, ClienteApi cliente) {
        String nombre1 = pedirNombre(teclado, "Nombre del jugador 1: ");
        String nombre2 = pedirNombre(teclado, "Nombre del jugador 2: ");
        if (nombre1.equalsIgnoreCase(nombre2)) {
            System.out.println("LOS DOS JUGADORES DEBEN TENER NOMBRES DISTINTOS");
            return;
        }

        if (!aplicarConfiguracion(cliente.obtenerConfiguracion())) {
            return;
        }

        String respuesta = cliente.crearPartida(nombre1, nombre2);
        if (respuesta == null) {
            return;
        }

        // datos que manda la API
        String partidaId = ClienteApi.valorDe(respuesta, "partidaId");
        String[] orden = ClienteApi.extraerJugadores(respuesta);
        Tablero tablero = new Tablero(ClienteApi.extraerTablero(respuesta));
        Casilla campamento = tablero.obtenerCasilla(ClienteApi.valorDe(respuesta, "campamento"));

        // todos empiezan en el campamento
        Jugador[] jugadores = new Jugador[2];
        jugadores[0] = new Jugador(orden[0], campamento.getFila(), campamento.getColumna());
        jugadores[1] = new Jugador(orden[1], campamento.getFila(), campamento.getColumna());

        ArrayList<String> ordenJugadores = new ArrayList<>();
        ordenJugadores.add(orden[0]);
        ordenJugadores.add(orden[1]);
        GestorTurnos gestor = new GestorTurnos();
        gestor.iniciarPartida(ordenJugadores);

        AccionesJugador acciones = new AccionesJugador(tablero, gestor, cliente, partidaId);

        System.out.println();
        System.out.println("PARTIDA CREADA: " + partidaId);
        System.out.println("El orden de juego es: 1) " + orden[0] + "  2) " + orden[1]);
        mostrarTablero(tablero, jugadores);

        boolean abandonar = false;
        while (!gestor.estaFinalizada() && !abandonar) {
            Jugador actual = buscarJugador(jugadores, gestor.obtenerJugadorActual());
            mostrarEstado(gestor, actual, tablero);

            System.out.println("1. Mover");
            System.out.println("2. Explorar templo (cuesta 1)");
            System.out.println("3. Entregar reliquias (cuesta 1)");
            System.out.println("4. Intentar abrir el cofre (cuesta 1)");
            System.out.println("5. Ver tablero");
            System.out.println("6. Ver inventario");
            System.out.println("7. Ver pistas");
            System.out.println("8. Terminar turno");
            System.out.println("0. Abandonar partida");
            System.out.print("Elige una opcion: ");
            String opcion = teclado.nextLine().trim();

            String turnoAntes = gestor.obtenerJugadorActual() + gestor.obtenerRonda();

            if (opcion.equals("1")) {
                System.out.print("Coordenada destino (ejemplo B2): ");
                Casilla destino = tablero.obtenerCasilla(teclado.nextLine().trim().toUpperCase());
                if (destino == null) {
                    System.out.println("ERROR. LA COORDENADA INGRESADA NO ES VALIDA: FUERA DE LOS LIMITES");
                } else {
                    acciones.procesarMovimiento(actual, destino.getFila(), destino.getColumna());
                }
            } else if (opcion.equals("2")) {
                acciones.procesarExploracion(actual);
            } else if (opcion.equals("3")) {
                acciones.procesarEntrega(actual);
            } else if (opcion.equals("4")) {
                acciones.procesarAperturaCofre(actual, teclado);
            } else if (opcion.equals("5")) {
                mostrarTablero(tablero, jugadores);
            } else if (opcion.equals("6")) {
                mostrarInventario(actual);
            } else if (opcion.equals("7")) {
                acciones.mostrarPistas();
            } else if (opcion.equals("8")) {
                gestor.terminarTurno();
            } else if (opcion.equals("0")) {
                abandonar = true;
            } else {
                System.out.println("OPCION NO VALIDA");
            }

            String turnoDespues = gestor.obtenerJugadorActual() + gestor.obtenerRonda();
            if (!abandonar && !gestor.estaFinalizada() && !turnoAntes.equals(turnoDespues)) {
                System.out.println();
                System.out.println("--- SE TERMINO EL TURNO DE " + actual.getNombreUsuario() + " ---");
            }
        }

        if (gestor.estaFinalizada()) {
            mostrarResultado(jugadores);
        } else {
            System.out.println("PARTIDA ABANDONADA");
        }
    }
    /**
     * Busca y devuelve la instancia de {@link Jugador} cuyo nombre de usuario coincida con el especificado.
     * 
     * @param jugadores Arreglo que contiene a los dos jugadores de la partida
     * @param nombre Nombre de usuario del jugador que se desea localizar
     * @return El objeto {@link Jugador} correspondiente
     */

    private static Jugador buscarJugador(Jugador[] jugadores, String nombre) {
        if (jugadores[0].getNombreUsuario().equals(nombre)) {
            return jugadores[0];
        }
        return jugadores[1];
    }

    /**
     * Imprime en consola el resumen del estado actual del turno: ronda en curso, jugador activo,
     * acciones disponibles, coordenada actual, tipo de casilla, reliquias cargadas y puntos acumulados
     * 
     * @param gestor Instancia de {@link GestorTurnos} con los datos de rondas y acciones
     * @param actual El {@link Jugador} que posee el turno actual
     * @param tablero El {@link Tablero} de la partida para consutar la casilla donde se encuentra el jugador
     */
    private static void mostrarEstado(GestorTurnos gestor, Jugador actual, Tablero tablero) {
        Casilla casilla = tablero.obtenerCasilla(actual.getFilaActual(), actual.getColumnaActual());
        System.out.println();
        System.out.println("RONDA " + gestor.obtenerRonda() + "/" + GestorTurnos.getRondasTotales()
                + " | TURNO DE: " + actual.getNombreUsuario()
                + " | ACCIONES: " + gestor.obtenerAccionesRestantes());
        System.out.println("Posicion: " + Tablero.convertirACoordenada(actual.getFilaActual(), actual.getColumnaActual())
                + " (" + casilla.getNombreCasilla() + ")"
                + " | Reliquias: " + actual.getCantidadReliquias() + "/" + Jugador.getCapacidadReliquias()
                + " | Puntos: " + actual.getPuntos());
    }

    /**
     * Muestra en consola el contenido del inventario de reliquias que lleva un jugador,
     * asi como el total de puntos y de reliquias ya entregadas en el campamento
     * 
     * @param jugador El {@link Jugador} cuyo inventario se desea visualizar
     */
    private static void mostrarInventario(Jugador jugador) {
        System.out.println("INVENTARIO DE " + jugador.getNombreUsuario() + ":");
        if (!jugador.tieneReliquias()) {
            System.out.println("  (vacio)");
        }
        Reliquia[] inventario = jugador.getInventarioReliquias();
        for (int i = 0; i < jugador.getCantidadReliquias(); i++) {
            System.out.println("  " + inventario[i].getId() + " - " + inventario[i].getPuntos() + " puntos");
        }
        System.out.println("Puntos entregados: " + jugador.getPuntos()
                + " | Reliquias entregadas: " + jugador.getReliquiasEntregadas());
    }


    /**
     * Dibuja en consola la cuadricula del tablero con coordenadas (columnas con letras, filas con numeros),
     * los símbolos de cada terreno, la ubicacion de ambos jugadores ({@code 1}, {@code 2} o {@code *})
     * y la leyenda
     * 
     * @param tablero El {@link Tablero} que contiene las casillas del mapa
     * @param jugadores Arreglo con los jugadores activos para mostrar sus posiciones
     */

    private static void mostrarTablero(Tablero tablero, Jugador[] jugadores) {
        String linea = "   .";
        String letras = "   ";
        for (int c = 0; c < Tablero.getColumnas(); c++) {
            linea = linea + "----.";
            letras = letras + "  " + (char) ('A' + c) + "  ";
        }
        System.out.println(letras);
        System.out.println(linea);
        for (int f = 0; f < Tablero.getFilas(); f++) {
            System.out.print(" " + (f + 1) + " |");
            for (int c = 0; c < Tablero.getColumnas(); c++) {
                boolean esta1 = jugadores[0].getFilaActual() == f && jugadores[0].getColumnaActual() == c;
                boolean esta2 = jugadores[1].getFilaActual() == f && jugadores[1].getColumnaActual() == c;
                String marca = " ";
                if (esta1 && esta2) {
                    marca = "*";
                } else if (esta1) {
                    marca = "1";
                } else if (esta2) {
                    marca = "2";
                }
                System.out.print(" " + tablero.obtenerCasilla(f, c).getSimbolo() + marca + " |");
            }
            System.out.println();
            System.out.println(linea);
        }
        System.out.println();
        System.out.println("C = Campamento");
        System.out.println("X = Cofre");
        System.out.println("T = Templo");
        System.out.println("P = Pantano");
        System.out.println("S = Sendero");
        System.out.println("1 = " + jugadores[0].getNombreUsuario() + " | 2 = " + jugadores[1].getNombreUsuario()
                + " | * = los dos juntos");
    }

    /**
     * Muestra en consola el marcador final de ambos jugadores al concluir las rondas de la partida
     * y determina el ganador comparando primero los puntos totales y en caso de igualdad, la cantidad de reliquias entregadas
     * 
     * @param jugadores Arreglo con los dos jugadores cuyos puntajes serán evaluados
     */

    private static void mostrarResultado(Jugador[] jugadores) {
        System.out.println();
        System.out.println("===== FIN DE LA PARTIDA =====");
        for (int i = 0; i < 2; i++) {
            System.out.println(jugadores[i].getNombreUsuario() + ": " + jugadores[i].getPuntos()
                    + " puntos, " + jugadores[i].getReliquiasEntregadas() + " reliquias entregadas");
        }

        Jugador a = jugadores[0];
        Jugador b = jugadores[1];
        if (a.getPuntos() > b.getPuntos()) {
            System.out.println("GANADOR: " + a.getNombreUsuario());
        } else if (b.getPuntos() > a.getPuntos()) {
            System.out.println("GANADOR: " + b.getNombreUsuario());
        } else if (a.getReliquiasEntregadas() > b.getReliquiasEntregadas()) {
            System.out.println("GANADOR POR DESEMPATE (MAS RELIQUIAS ENTREGADAS): " + a.getNombreUsuario());
        } else if (b.getReliquiasEntregadas() > a.getReliquiasEntregadas()) {
            System.out.println("GANADOR POR DESEMPATE (MAS RELIQUIAS ENTREGADAS): " + b.getNombreUsuario());
        } else {
            System.out.println("EMPATE: AMBOS COMPARTEN LA VICTORIA");
        }
    }
}

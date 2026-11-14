package juego;

import SistemaTurnos.AccionesJugador;
import SistemaTurnos.GestorTurnos;
import casillas.Casilla;
import casillas.Tablero;
import jugador.Jugador;
import objetos.Reliquia;

import java.util.ArrayList;
import java.util.Scanner;

// Menu principal y ciclo del juego en consola
public class Juego {

    public static final String URL_API = "http://127.0.0.1:8000";

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

    private static void nuevaPartida(Scanner teclado, ClienteApi cliente) {
        String nombre1 = pedirNombre(teclado, "Nombre del jugador 1: ");
        String nombre2 = pedirNombre(teclado, "Nombre del jugador 2: ");
        if (nombre1.equalsIgnoreCase(nombre2)) {
            System.out.println("LOS DOS JUGADORES DEBEN TENER NOMBRES DISTINTOS");
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

    private static Jugador buscarJugador(Jugador[] jugadores, String nombre) {
        if (jugadores[0].getNombreUsuario().equals(nombre)) {
            return jugadores[0];
        }
        return jugadores[1];
    }

    private static void mostrarEstado(GestorTurnos gestor, Jugador actual, Tablero tablero) {
        Casilla casilla = tablero.obtenerCasilla(actual.getFilaActual(), actual.getColumnaActual());
        System.out.println();
        System.out.println("RONDA " + gestor.obtenerRonda() + "/" + GestorTurnos.RONDAS_TOTALES
                + " | TURNO DE: " + actual.getNombreUsuario()
                + " | ACCIONES: " + gestor.obtenerAccionesRestantes());
        System.out.println("Posicion: " + Tablero.convertirACoordenada(actual.getFilaActual(), actual.getColumnaActual())
                + " (" + casilla.getNombreCasilla() + ")"
                + " | Reliquias: " + actual.getCantidadReliquias() + "/" + Jugador.CAPACIDAD_RELIQUIAS
                + " | Puntos: " + actual.getPuntos());
    }

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

    private static void mostrarTablero(Tablero tablero, Jugador[] jugadores) {
        String linea = "   .----.----.----.----.----.";
        System.out.println("     A    B    C    D    E");
        System.out.println(linea);
        for (int f = 0; f < Tablero.FILAS; f++) {
            System.out.print(" " + (f + 1) + " |");
            for (int c = 0; c < Tablero.COLUMNAS; c++) {
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

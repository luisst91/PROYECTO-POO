package SistemaTurnos;

import casillas.Campamento;
import casillas.Casilla;
import casillas.Cofre;
import casillas.Tablero;
import casillas.Templo;
import jugador.Jugador;
import juego.ClienteApi;
import juego.Descifrador;
import objetos.Pista;
import objetos.Reliquia;

import java.util.ArrayList;
import java.util.Scanner;
/**
 * Controlador encargado de ejecutar y validar las distintas acciones que un jugador
 * puede realizar durante su turno: desplazarse por el tablero, explorar templos,
 * entregar reliquias en el campamento e intentar descifrar y abrir el cofre.
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 */

public class AccionesJugador {
    /**
     * Referencia al tablero de juego donde se ubican las casillas y los jugadores.
     */
    private Tablero tablero;
    /**
     * Gestor encarga de validar y descontar los puntos de accion disponibles en el turno
     */
    private GestorTurnos gestorTurnos;
    /**
     * Cliente HTTP usado para consultar informacion de templos y cofres al servidor
     */
    private ClienteApi cliente;
    /**
     * Identificador único de la partida en curso en el servidor
     */
    private String partidaId;
    /**
     * Lista compartida de pistas descubiertas al exploraer los templos durante la partida
     */
    private ArrayList<Pista> pistasConocidas = new ArrayList<>();
    /**
     * Constrye una nueva instancia del controlador de acciones del jugador
     * 
     * @param tablero      El {@link Tablero} activo de la partida.
     * @param gestorTurnos El {@link GestorTurnos} que controla las acciones restantes.
     * @param cliente      La instancia de {@link ClienteApi} para comunicarse con el backend.
     * @param partidaId    El identificador único de la partida actual.
     */
    public AccionesJugador(Tablero tablero, GestorTurnos gestorTurnos, ClienteApi cliente, String partidaId) {
        this.tablero = tablero;
        this.gestorTurnos = gestorTurnos;
        this.cliente = cliente;
        this.partidaId = partidaId;
    }

    /**
     * Obtiene la lista de pistas que han sido descubiertas hasta el momento en la partida
     * @return Un {@link ArrayList} con los objetos {@link Pista} conocidos
     */
    public ArrayList<Pista> getPistasConocidas() {
        return pistasConocidas;
    }

    /**
     * Valida y ejecuta el desplazamiento de un jugador hacia una casilla destino.
     * Comprueba que la coordenada exista, que la casilla esté justo al lado del jugador
     * y que disponga de suficientes puntos de acción para cubrir el costo del terreno.
     *
     * @param jugadorActual El {@link Jugador} que intenta realizar el movimiento.
     * @param filaNueva     Índice de la fila de la casilla destino.
     * @param columnaNueva  Índice de la columna de la casilla destino.
     */
    public void procesarMovimiento(Jugador jugadorActual, int filaNueva, int columnaNueva){

        Casilla destino = tablero.obtenerCasilla(filaNueva, columnaNueva);
        if (destino == null){
            System.out.println("ERROR. LA COORDENADA INGRESADA NO ES VALIDA: FUERA DE LOS LIMITES");
        }
        else if (!jugadorActual.esVecina(destino)){
            System.out.println("ERROR. SOLO PUEDES MOVERTE A UNA CASILLA VECINA (ARRIBA, ABAJO, IZQUIERDA O DERECHA)");
        }
        else{
            if (gestorTurnos.gastarAcciones(destino.obtenerCostoMovimiento())){
                jugadorActual.moverJugador(destino);
                System.out.println("MOVIMIENTO EXITOSO A: " + destino.getNombreCasilla());
            }
            else{
                System.out.println("NO TIENES ACCIONES SUFICIENTES PARA REALIZAR EL MOVIMIENTO");
            }
        }
    }

    // explorar un templo y recoger su reliquia (cuesta 1 accion)

    /**
     * Procesa la exploración de un {@link Templo} en la posición actual del jugador (cuesta 1 acción).
     * Consulta la API para obtener la {@link Reliquia} oculta y, si el templo contiene un fragmento
     * de clave, registra la nueva {@link Pista} en la lista de pistas conocidas.
     *
     * @param jugadorActual El {@link Jugador} que intenta explorar el templo.
     */
    public void procesarExploracion(Jugador jugadorActual){
        Casilla actual = tablero.obtenerCasilla(jugadorActual.getFilaActual(), jugadorActual.getColumnaActual());

        if (!(actual instanceof Templo)){
            System.out.println("SOLO PUEDES EXPLORAR CUANDO ESTAS EN UN TEMPLO (T)");
            return;
        }
        Templo templo = (Templo) actual;

        if (templo.isExplorado()){
            System.out.println("ESTE TEMPLO YA FUE EXPLORADO");
            return;
        }
        if (!jugadorActual.tieneEspacio()){
            System.out.println("TU INVENTARIO ESTA LLENO. ENTREGA TUS RELIQUIAS EN EL CAMPAMENTO");
            return;
        }
        if (!gestorTurnos.puedeGastarAcciones(1)){
            System.out.println("NO TIENES ACCIONES SUFICIENTES PARA EXPLORAR");
            return;
        }

        String coordenada = Tablero.convertirACoordenada(templo.getFila(), templo.getColumna());
        String json = cliente.obtenerTemplo(partidaId, coordenada);
        if (json == null){
            System.out.println("NO SE PUDO CONSULTAR EL TEMPLO. NO SE GASTARON ACCIONES");
            return;
        }

        String idReliquia = ClienteApi.valorDe(json, "reliquiaId");
        int valor = Integer.parseInt(ClienteApi.valorDe(json, "valor"));
        Reliquia reliquia = new Reliquia(idReliquia, "Reliquia " + idReliquia, valor);

        gestorTurnos.gastarAcciones(1);
        templo.setExplorado(true);
        jugadorActual.agregarReliquia(reliquia);
        System.out.println("EXPLORASTE EL TEMPLO Y RECOGISTE " + reliquia.getId() + " (" + valor + " PUNTOS)");

        if (json.indexOf("\"fragmento\"") != -1){
            int indice = Integer.parseInt(ClienteApi.valorDe(json, "indice"));
            String fragmento = ClienteApi.valorDe(json, "fragmento");
            Pista pista = new Pista(indice, fragmento);
            templo.setPista(pista);
            pistasConocidas.add(pista);
            System.out.println("ENCONTRASTE LA PISTA " + indice + ": " + fragmento);
        }
    }

    // entregar todas las reliquias en el campamento (cuesta 1 accion)

    /**
     * Procesa la entrega de todas las reliquias que lleva el jugador cuando se encuentra
     * ubicado en la casilla de {@link Campamento} (cuesta 1 acción), sumando los puntos correspondientes.
     *
     * @param jugadorActual El {@link Jugador} que desea depositar sus reliquias.
     */
    public void procesarEntrega(Jugador jugadorActual){
        Casilla actual = tablero.obtenerCasilla(jugadorActual.getFilaActual(), jugadorActual.getColumnaActual());

        if (!(actual instanceof Campamento)){
            System.out.println("SOLO PUEDES ENTREGAR RELIQUIAS EN EL CAMPAMENTO (C)");
            return;
        }
        if (!jugadorActual.tieneReliquias()){
            System.out.println("NO LLEVAS NINGUNA RELIQUIA PARA ENTREGAR");
            return;
        }
        if (!gestorTurnos.puedeGastarAcciones(1)){
            System.out.println("NO TIENES ACCIONES SUFICIENTES PARA ENTREGAR");
            return;
        }

        gestorTurnos.gastarAcciones(1);
        int cantidad = jugadorActual.getCantidadReliquias();
        int ganados = jugadorActual.entregarReliquia();
        System.out.println("ENTREGASTE " + cantidad + " RELIQUIA(S) Y GANASTE " + ganados + " PUNTOS");
    }

    // intentar abrir el cofre con la contrasena (cuesta 1 accion si el formato es valido)

    /**
     * Gestiona el intento de apertura del {@link Cofre} cuando el jugador está sobre su casilla.
     * Verifica que se conozcan al menos 2 pistas, valida el formato de la clave ingresada por consola
     * (consumiendo 1 acción si el formato es válido) y utiliza {@link Descifrador} para autenticar
     * el paquete del servidor y otorgar la reliquia especial si la clave es correcta.
     *
     * @param jugadorActual El {@link Jugador} que intenta abrir el cofre.
     * @param teclado       Instancia de {@link Scanner} para leer la contraseña ingresada por el usuario.
     */
    public void procesarAperturaCofre(Jugador jugadorActual, Scanner teclado){
        Casilla actual = tablero.obtenerCasilla(jugadorActual.getFilaActual(), jugadorActual.getColumnaActual());

        if (!(actual instanceof Cofre)){
            System.out.println("SOLO PUEDES ABRIR EL COFRE CUANDO ESTAS EN EL (X)");
            return;
        }
        Cofre cofre = (Cofre) actual;

        if (cofre.isAbierto()){
            System.out.println("EL COFRE YA ESTA ABIERTO");
            return;
        }
        if (pistasConocidas.size() < 2){
            System.out.println("NECESITAS CONOCER LAS DOS PISTAS PARA INTENTAR ABRIR EL COFRE");
            return;
        }
        if (!jugadorActual.tieneEspacio()){
            System.out.println("TU INVENTARIO ESTA LLENO. ENTREGA TUS RELIQUIAS EN EL CAMPAMENTO");
            return;
        }
        if (!gestorTurnos.puedeGastarAcciones(1)){
            System.out.println("NO TIENES ACCIONES SUFICIENTES PARA ABRIR EL COFRE");
            return;
        }

        mostrarPistas();
        System.out.print("Escribe la contrasena (une las pistas con un guion): ");
        String clave = teclado.nextLine().toUpperCase();

        if (!formatoValido(clave)){
            System.out.println("FORMATO INVALIDO (4 A 20 CARACTERES: LETRAS, NUMEROS Y UN GUION). NO SE GASTARON ACCIONES");
            return;
        }

        String paquete = cliente.obtenerCofre(partidaId);
        if (paquete == null){
            System.out.println("NO SE PUDO CONSULTAR EL COFRE. NO SE GASTARON ACCIONES");
            return;
        }

        // el formato es valido, asi que el intento cuesta 1 accion aunque falle
        gestorTurnos.gastarAcciones(1);

        String datos = Descifrador.descifrar(paquete, clave);
        boolean coincide = datos != null
                && datos.indexOf("\"partidaId\":\"" + ClienteApi.valorDe(paquete, "partidaId") + "\"") != -1
                && datos.indexOf("\"cofreId\":\"" + ClienteApi.valorDe(paquete, "cofreId") + "\"") != -1;

        if (!coincide){
            System.out.println("NO SE PUDO AUTENTICAR EL ARCHIVO. CONTRASENA INCORRECTA (SE GASTO 1 ACCION)");
            return;
        }

        String idReliquia = ClienteApi.valorDe(datos, "reliquiaId");
        int valor = Integer.parseInt(ClienteApi.valorDe(datos, "valor"));
        cofre.setAbierto(true);
        jugadorActual.agregarReliquia(new Reliquia(idReliquia, "Reliquia especial", valor));
        System.out.println("COFRE ABIERTO! RECIBISTE " + idReliquia + " (" + valor + " PUNTOS)");
    }

    /**
     * Imprime en consola todas las pistas que han sido descubiertas hasta el momento en la partida,
     * o un aviso si aún no se ha encontrado ninguna.
     */
    public void mostrarPistas(){
        if (pistasConocidas.size() == 0){
            System.out.println("TODAVIA NO CONOCEN NINGUNA PISTA");
        }
        for (int i = 0; i < pistasConocidas.size(); i++){
            Pista pista = pistasConocidas.get(i);
            System.out.println("Pista " + pista.getNumeroPista() + ": " + pista.getContenido());
        }
    }

    // formato: letras mayusculas y numeros, con maximo un guion en medio, de 4 a 20 caracteres
    
    /**
     * Valida que la contraseña ingresada cumpla con las reglas de formato exigidas:
     * tener entre 4 y 20 caracteres, contener únicamente letras mayúsculas (A-Z), números (0-9)
     * y como máximo un guion ({@code -}) ubicado en una posición intermedia (ni al inicio ni al final).
     *
     * @param clave Cadena en mayúsculas ingresada por el jugador.
     * @return {@code true} si la cadena respeta el formato permitido; {@code false} en caso contrario.
     */
    private boolean formatoValido(String clave){
        if (clave.length() < 4 || clave.length() > 20){
            return false;
        }
        int guiones = 0;
        for (int i = 0; i < clave.length(); i++){
            char c = clave.charAt(i);
            if (c == '-'){
                guiones++;
                if (i == 0 || i == clave.length() - 1){
                    return false;
                }
            }
            else if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))){
                return false;
            }
        }
        return guiones <= 1;
    }
}

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

public class AccionesJugador {
    /**
     * Puntos de acción que cuesta explorar un templo. No tiene un valor fijo: se coloca con
     * {@link #setCostoExplorar(int)} con el valor que entrega la API.
     */
    private static int costoExplorar;
    /**
     * Puntos de acción que cuesta entregar las reliquias en el campamento. Lo entrega la API.
     */
    private static int costoEntregar;
    /**
     * Puntos de acción que cuesta intentar abrir el cofre. Lo entrega la API.
     */
    private static int costoAbrirCofre;

    private Tablero tablero;
    private GestorTurnos gestorTurnos;
    private ClienteApi cliente;
    private String partidaId;
    private ArrayList<Pista> pistasConocidas = new ArrayList<>();

    /**
     * Devuelve el costo en puntos de acción de explorar un templo.
     *
     * @return El costo de explorar
     */
    public static int getCostoExplorar() {
        return costoExplorar;
    }

    /**
     * Coloca el costo de explorar un templo.
     *
     * @param nuevoCosto Nuevo costo que entrega la API
     */
    public static void setCostoExplorar(int nuevoCosto) {
        costoExplorar = nuevoCosto;
    }

    /**
     * Devuelve el costo en puntos de acción de entregar las reliquias.
     *
     * @return El costo de entregar
     */
    public static int getCostoEntregar() {
        return costoEntregar;
    }

    /**
     * Coloca el costo de entregar las reliquias en el campamento.
     *
     * @param nuevoCosto Nuevo costo que entrega la API
     */
    public static void setCostoEntregar(int nuevoCosto) {
        costoEntregar = nuevoCosto;
    }

    /**
     * Devuelve el costo en puntos de acción de intentar abrir el cofre.
     *
     * @return El costo de abrir el cofre
     */
    public static int getCostoAbrirCofre() {
        return costoAbrirCofre;
    }

    /**
     * Coloca el costo de intentar abrir el cofre.
     *
     * @param nuevoCosto Nuevo costo que entrega la API
     */
    public static void setCostoAbrirCofre(int nuevoCosto) {
        costoAbrirCofre = nuevoCosto;
    }

    public AccionesJugador(Tablero tablero, GestorTurnos gestorTurnos, ClienteApi cliente, String partidaId) {
        this.tablero = tablero;
        this.gestorTurnos = gestorTurnos;
        this.cliente = cliente;
        this.partidaId = partidaId;
    }

    public ArrayList<Pista> getPistasConocidas() {
        return pistasConocidas;
    }

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
        if (!gestorTurnos.puedeGastarAcciones(costoExplorar)){
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

        gestorTurnos.gastarAcciones(costoExplorar);
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
        if (!gestorTurnos.puedeGastarAcciones(costoEntregar)){
            System.out.println("NO TIENES ACCIONES SUFICIENTES PARA ENTREGAR");
            return;
        }

        gestorTurnos.gastarAcciones(costoEntregar);
        int cantidad = jugadorActual.getCantidadReliquias();
        int ganados = jugadorActual.entregarReliquia();
        System.out.println("ENTREGASTE " + cantidad + " RELIQUIA(S) Y GANASTE " + ganados + " PUNTOS");
    }

    // intentar abrir el cofre con la contrasena (cuesta 1 accion si el formato es valido)
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
        if (!gestorTurnos.puedeGastarAcciones(costoAbrirCofre)){
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
        gestorTurnos.gastarAcciones(costoAbrirCofre);

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

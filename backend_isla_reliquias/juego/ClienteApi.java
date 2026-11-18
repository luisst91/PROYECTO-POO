package juego;

import casillas.Tablero;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente HTTP encargado de gestionar la comunicación entre el juego en Java y el backend(API en python).
 * Permite crear nuevas partidas, consultar la información de templos y cofres, y procesar las respuestas.
 * en formato JSON recibidas del servidor
 * 
 * @author Ernesto Gutiérrez, Diego Nieto, Luis Torres
 * @version 1.0
 * 
 */

public class ClienteApi {

    /**
     * URL base del servidor al que se realizarán las peticiones HTTP
     */
    private String urlBase;
    /**
     * Instania del cliente HTTP configurada bajo el protocolo HTTP/1.1 para enviar y recibir peticiones.
     */
    private HttpClient cliente;

    /**
     * Construye una nueva instancia del cliente apuntando a la URL base especificada.
     * Confirgura internament el cliente HTTP usando la version HTTP/1.1 para asegurar compatibilidad con Uvicorn.
     * 
     * @param urlBase
     */

    public ClienteApi(String urlBase) {
        this.urlBase = urlBase;
        // HTTP/1.1 para que uvicorn reciba el body
        this.cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    /**
     * Envía una peticion POST al servidor para crear una nueva partida con dos jugadores.
     * 
     * @param jugador1 Nombre del primero jugador
     * @param jugador2 Nombre del segundo jugador
     * @return El cuerpo de la respuesta en formato JSON enviado por el servidor con los datos inicales de la partida, o {@code null} si ocurrió un error de conexión.
     */
    public String crearPartida(String jugador1, String jugador2) {
        String json = "{\"jugadores\":[\"" + jugador1 + "\",\"" + jugador2 + "\"]}";
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/partidas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return enviar(peticion);
    }

    /**
     * Consulta al servidor la información específica de un templo en una coordenada especifica de la partida.
     * 
     * @param partidaId Identificador único de la partida actual
     * @param coordenada Coordenada del templo en el tablero
     * @return El cuerpo de la respuesta en formato JSON con los detalles del templo, o {@code null} si falla la petición.
     */
    public String obtenerTemplo(String partidaId, String coordenada) {
        return pedir("/partidas/" + partidaId + "/templos/" + coordenada);
    }

    /**
     * Consulta al servidor la información del cofre correspondiente a la partida
     * 
     * @param partidaId Identificador único de la partida actual
     * @return El cuerpo de la respuesta en formato JSON con los detalles del cofre, o {@code null} si falla la petición
     */

    public String obtenerCofre(String partidaId) {
        return pedir("/partidas/" + partidaId + "/cofre");
    }

    /**
     * Consulta al servidor los valores de configuración del juego (tamaño del tablero, rondas, acciones por turno,
     * capacidad del inventario y costos de las acciones). Es el método que SACA los valores de la API;
     * para colocarlos en las clases se usa {@code Juego.aplicarConfiguracion(String)}.
     *
     * @return El cuerpo de la respuesta en formato JSON con la configuración, o {@code null} si falla la petición
     */
    public String obtenerConfiguracion() {
        return pedir("/configuracion");
    }

    /**
     * Construye y ejecuta una petición HTTP de tipo GET hacia una ruta relativa de la API.
     * 
     * @param ruta El endpoint relativo que se concatenará a {@link #urlBase}
     * @return El texto de la respuesta del servidor, o {@code null} si ocurre un error
     */
    private String pedir(String ruta) {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + ruta))
                .GET()
                .build();
        return enviar(peticion);
    }

    /**
     * Envía una petición HTTP previamente construida y procesa la respuesta del servidor
     * Verifica que el codigo de estado sea {@code 200} (OK); de lo contrario, muestra un mensaje de error por consola.
     * 
     * @param peticion La solicitud {@link HttpRequest} lista para ser enviada
     * @return El contenido en texto plano del cuerpo de la respuesta, o {@code null} si hubo un error
     */
    private String enviar(HttpRequest peticion) {
        try {
            HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                System.out.println("ERROR DEL SERVIDOR: " + respuesta.statusCode());
                System.out.println(respuesta.body());
                return null;
            }
            return respuesta.body();
        } catch (Exception e) {
            System.out.println("NO SE PUDO CONECTAR CON EL SERVIDOR. ESTA ENCENDIDO?");
            return null;
        }
    }


    /**
     * Extrae el valor asociado a una clave específica dentro de una cadena en formato JSON
     * Por ejemplo, ante {@code "campamento":"C3"} y la clave {@code "campamento"}, devuelve {@code "C3"}
     * 
     * @param json La cadena de texto en formato JSON donde se realizará la busqueda
     * @param clave El nombre de la clave cuyo valor se desea obtener
     * @return El valor encontrado en formato texto, o {@code null} si la clave no existe en el JSON
     */
    public static String valorDe(String json, String clave) {
        int posicion = json.indexOf("\"" + clave + "\"");
        if (posicion == -1) {
            return null;
        }
        posicion = json.indexOf(":", posicion) + 1;
        while (json.charAt(posicion) == ' ') {
            posicion++;
        }
        if (json.charAt(posicion) == '"') {
            int fin = json.indexOf("\"", posicion + 1);
            return json.substring(posicion + 1, fin);
        }
        int fin = posicion;
        while (fin < json.length() && json.charAt(fin) != ',' && json.charAt(fin) != '}' && json.charAt(fin) != ']') {
            fin++;
        }
        return json.substring(posicion, fin);
    }

    /**
     * Analiza una cadena JSON y extrae los caracteres en mayúsculas que representan las casillas del tablero,
     * organizándolos en una matriz 2D del tamaño configurado del tablero
     * 
     * @param json La cadena de texo en formato JSON que contiene la clave {@code "tablero"}
     * @return Una matriz de caracteres de dimensiones filas X columnas con la distribución del mapa
     */
    public static char[][] extraerTablero(String json) {
        int inicio = json.indexOf("\"tablero\"");
        int fin = json.indexOf("]]", inicio);
        String tablero = json.substring(inicio, fin);

        char[][] matriz = new char[Tablero.getFilas()][Tablero.getColumnas()];
        int contador = 0;
        for (int i = 0; i < tablero.length(); i++) {
            char c = tablero.charAt(i);
            if (Character.isUpperCase(c)) {
                matriz[contador / Tablero.getColumnas()][contador % Tablero.getColumnas()] = c;
                contador++;
            }
        }
        return matriz;
    }

    /**
     * Extrae del JSON los nombres de los dos jugadores participante en el orden random determinado por la API
     * 
     * @param json La cadena de texto en formato JSON que contiene el arreglo {@code "jugadores"}
     * @return Un arreglo de {@link Sring} de longitud 2 con los nombres de ambos jugadores
     */
    public static String[] extraerJugadores(String json) {
        int inicio = json.indexOf("[", json.indexOf("\"jugadores\""));
        int a = json.indexOf("\"", inicio);
        int b = json.indexOf("\"", a + 1);
        int c = json.indexOf("\"", b + 1);
        int d = json.indexOf("\"", c + 1);
        return new String[]{json.substring(a + 1, b), json.substring(c + 1, d)};
    }
}

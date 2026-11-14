package juego;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Se encarga de hablar con la API de Python
public class ClienteApi {

    private String urlBase;
    private HttpClient cliente;

    public ClienteApi(String urlBase) {
        this.urlBase = urlBase;
        // HTTP/1.1 para que uvicorn reciba el body
        this.cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }

    public String crearPartida(String jugador1, String jugador2) {
        String json = "{\"jugadores\":[\"" + jugador1 + "\",\"" + jugador2 + "\"]}";
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/partidas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return enviar(peticion);
    }

    public String obtenerTemplo(String partidaId, String coordenada) {
        return pedir("/partidas/" + partidaId + "/templos/" + coordenada);
    }

    public String obtenerCofre(String partidaId) {
        return pedir("/partidas/" + partidaId + "/cofre");
    }

    private String pedir(String ruta) {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + ruta))
                .GET()
                .build();
        return enviar(peticion);
    }

    // devuelve el texto de la respuesta, o null si algo fallo
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

    // Busca "clave" en el JSON y devuelve su valor como texto. Ejemplo: "campamento":"C3" -> C3
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

    // saca las 25 letras del tablero: desde "tablero" hasta "]]"
    public static char[][] extraerTablero(String json) {
        int inicio = json.indexOf("\"tablero\"");
        int fin = json.indexOf("]]", inicio);
        String tablero = json.substring(inicio, fin);

        char[][] matriz = new char[5][5];
        int contador = 0;
        for (int i = 0; i < tablero.length(); i++) {
            char c = tablero.charAt(i);
            if (Character.isUpperCase(c)) {
                matriz[contador / 5][contador % 5] = c;
                contador++;
            }
        }
        return matriz;
    }

    // devuelve los dos jugadores en el orden que sorteo la API
    public static String[] extraerJugadores(String json) {
        int inicio = json.indexOf("[", json.indexOf("\"jugadores\""));
        int a = json.indexOf("\"", inicio);
        int b = json.indexOf("\"", a + 1);
        int c = json.indexOf("\"", b + 1);
        int d = json.indexOf("\"", c + 1);
        return new String[]{json.substring(a + 1, b), json.substring(c + 1, d)};
    }
}

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class conexionPythonJava {
    public static void main(String[] args) throws Exception {
        String jugador1 = "J-ID-1";
        String jugador2 = "J-ID-2";

        String json = "{\"requestId\":\"" + UUID.randomUUID() + "\",\"jugadores\":[\"" + jugador1 + "\",\"" + jugador2 + "\"]}";

        // HTTP/1.1 para que uvicorn reciba el body
        HttpClient cliente = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8000/api/v1/partidas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 201) {
            System.out.println("ERROR DEL SERVIDOR: " + respuesta.statusCode());
            System.out.println(respuesta.body());
            return;
        }

        // se recorta solo el texto del tablero: desde "tablero" hasta "]]"
        String texto = respuesta.body();
        int inicio = texto.indexOf("\"tablero\"");
        int fin = texto.indexOf("]]", inicio);
        String tablero = texto.substring(inicio, fin);

        // casillas
        String linea = "   .----.----.----.----.----.";

        System.out.println("     A    B    C    D    E");
        System.out.println(linea);

        int contador = 0;
        int i = 0;

        while (i < tablero.length()) {
            char c = tablero.charAt(i);

            if (Character.isUpperCase(c)) {
                if (contador % 5 == 0) {
                    System.out.print(" " + (contador / 5 + 1) + " |");
                }

                System.out.print("  " + c + " |");
                contador++;

                if (contador % 5 == 0) {
                    System.out.println();
                    System.out.println(linea);
                }
            }

            i++;
        }

        System.out.println("");
        System.out.println("C = Campamento");
        System.out.println("X = Cofre");
        System.out.println("T = Templo");
        System.out.println("P = Pantano");
        System.out.println("S = Sendero");
        System.out.println("S = Sendero");
        System.out.println("");
    }
}

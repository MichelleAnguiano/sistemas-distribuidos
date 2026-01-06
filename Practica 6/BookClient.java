// BookClient.java
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class BookClient {

    private static final String BASE_URL = "http://localhost:8080/books";

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        //  GET /books
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Respuesta GET:\n" + getResponse.body());

        // POST /books
        String json = "{\"id\":1,\"categoria\":\"Programación\",\"titulo\":\"Sistemas Distribuidos\"}";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nRespuesta POST:\n" + postResponse.body());

        // GET nuevamente para verificar
        HttpResponse<String> getResponse2 = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("\nLista de libros actualizada:\n" + getResponse2.body());
    }
}

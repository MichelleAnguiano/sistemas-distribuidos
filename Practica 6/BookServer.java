// BookServer.java
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class BookServer {

    private static final List<Book> books = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Servidor iniciado en http://localhost:" + port);

        // GET /books  → lista todos los libros
        server.createContext("/books", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET":
                        handleGet(exchange);
                        break;
                    case "POST":
                        handlePost(exchange);
                        break;
                    default:
                        handleUnsupported(exchange);
                }
            }
        });

        server.setExecutor(null); // usa el default
        server.start();
    }

    private static void handleGet(HttpExchange exchange) throws IOException {
        String response = books.stream()
                .map(Book::toString)
                .collect(Collectors.joining(",", "[", "]"));
        sendResponse(exchange, 200, response);
    }

    private static void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        // parse simple JSON (sin librerías externas)
        try {
            Map<String, String> map = parseJson(json);
            Book newBook = new Book(
                    Integer.parseInt(map.get("id")),
                    map.get("categoria"),
                    map.get("titulo"));
            books.add(newBook);
            sendResponse(exchange, 201, "{\"status\":\"Libro agregado\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"JSON inválido\"}");
        }
    }

    private static void handleUnsupported(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\":\"Método no permitido\"}");
    }

    private static void sendResponse(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseJson(String json) {
        json = json.trim().replaceAll("[{}\"]", "");
        Map<String, String> map = new HashMap<>();
        for (String pair : json.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }
}


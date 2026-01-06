package src;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookServer {
    // Config
    private static final int PORT = 8080;
    private static final Path STATIC_DIR = Paths.get("static");
    private static final Path DATA_FILE = Paths.get("data", "books.json");

    // Estado en memoria (se sincroniza con disco)
    private static final List<Book> BOOKS = Collections.synchronizedList(new ArrayList<>());
    private static int NEXT_ID = 1;

    public static void main(String[] args) throws Exception {
        ensureDataFile();
        loadFromDisk();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API REST
        server.createContext("/api/books", exchange -> {
            addCORS(exchange);
            try {
                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleListBooks(exchange);
                    case "POST" -> handleCreateBook(exchange);
                    case "OPTIONS" -> options(exchange);
                    default -> sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJSON(exchange, 500, "{\"error\":\"Internal error\"}");
            } finally {
                exchange.close();
            }
        });

        // Service Worker en raíz (content-type correcto)
        server.createContext("/sw.js", exchange -> {
            addCORS(exchange);
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed", "text/plain");
                return;
            }
            Path p = STATIC_DIR.resolve("sw.js");
            sendFile(exchange, p, "application/javascript");
        });

        // Archivos estáticos y SPA fallback
        server.createContext("/", exchange -> {
            addCORS(exchange);
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed", "text/plain");
                return;
            }
            String rawPath = exchange.getRequestURI().getPath();
            String path = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
            Path file = STATIC_DIR.resolve(path.substring(1)).normalize();

            if (path.equals("/")) file = STATIC_DIR.resolve("index.html");

            // Evitar path traversal
            if (!file.startsWith(STATIC_DIR)) {
                sendText(exchange, 403, "Forbidden", "text/plain");
                return;
            }

            if (Files.exists(file) && !Files.isDirectory(file)) {
                String ctype = contentType(file);
                sendFile(exchange, file, ctype);
            } else {
                // fallback SPA
                sendFile(exchange, STATIC_DIR.resolve("index.html"), "text/html; charset=utf-8");
            }
        });

        server.setExecutor(null);
        System.out.println("PWA Books corriendo en http://127.0.0.1:" + PORT);
        server.start();
    }

    // ====== Handlers API ======
    private static void handleListBooks(HttpExchange ex) throws IOException {
        String json = booksToJson(BOOKS);
        sendJSON(ex, 200, json);
    }

    private static void handleCreateBook(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        // Parseo JSON muy simple (sin libs): {"categoria":"X","titulo":"Y"}
        // opcionalmente puede venir "id", se ignora y se reasigna
        Map<String, String> map = parseJsonFlat(body);

        String categoria = opt(map.get("categoria")).trim();
        String titulo = opt(map.get("titulo")).trim();

        if (titulo.isEmpty()) {
            sendJSON(ex, 400, "{\"error\":\"El título es obligatorio\"}");
            return;
        }
        int id = NEXT_ID++;
        Book b = new Book(id, categoria, titulo);
        BOOKS.add(b);
        saveToDisk();

        sendJSON(ex, 201, bookToJson(b));
    }

    private static void options(HttpExchange ex) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(204, -1);
    }

    // ====== Modelo ======
    static class Book {
        int id;
        String categoria;
        String titulo;
        Book(int id, String categoria, String titulo) {
            this.id = id; this.categoria = categoria; this.titulo = titulo;
        }
    }

    // ====== Persistencia simple (JSON plano) ======
    private static void ensureDataFile() throws IOException {
        if (!Files.exists(DATA_FILE.getParent())) Files.createDirectories(DATA_FILE.getParent());
        if (!Files.exists(DATA_FILE)) {
            Files.writeString(DATA_FILE, "[]", StandardCharsets.UTF_8);
        }
    }

    private static synchronized void loadFromDisk() throws IOException {
        String raw = Files.readString(DATA_FILE, StandardCharsets.UTF_8).trim();
        if (raw.isEmpty()) raw = "[]";
        List<Book> tmp = new ArrayList<>();
        // Parseo MUY básico de array de objetos (solo campos id/categoria/titulo)
        Pattern objP = Pattern.compile("\\{([^}]*)\\}");
        Matcher m = objP.matcher(raw);
        int maxId = 0;
        while (m.find()) {
            Map<String,String> map = parseJsonFlat("{"+m.group(1)+"}");
            int id = toInt(map.get("id"));
            String categoria = opt(map.get("categoria"));
            String titulo = opt(map.get("titulo"));
            if (id > maxId) maxId = id;
            tmp.add(new Book(id, categoria, titulo));
        }
        BOOKS.clear();
        BOOKS.addAll(tmp);
        NEXT_ID = Math.max(1, maxId + 1);
    }

    private static synchronized void saveToDisk() throws IOException {
        String json = booksToJson(BOOKS);
        Files.writeString(DATA_FILE, json, StandardCharsets.UTF_8);
    }

    // ====== Utilidades JSON minimalistas ======
    private static Map<String,String> parseJsonFlat(String json) {
        Map<String,String> map = new HashMap<>();
        // extrae "key":"value" o "key":123 (sin anidación)
        Pattern pStr = Pattern.compile("\"(.*?)\"\\s*:\\s*\"(.*?)\"");
        Matcher ms = pStr.matcher(json);
        while (ms.find()) {
            map.put(ms.group(1), unescape(ms.group(2)));
        }
        Pattern pNum = Pattern.compile("\"(.*?)\"\\s*:\\s*(\\d+)");
        Matcher mn = pNum.matcher(json);
        while (mn.find()) {
            map.put(mn.group(1), mn.group(2));
        }
        return map;
    }

    private static String escape(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"")
                .replace("\n","\\n").replace("\r","\\r");
    }
    private static String unescape(String s) {
        return s.replace("\\n","\n").replace("\\r","\r")
                .replace("\\\"","\"").replace("\\\\","\\");
    }
    private static String bookToJson(Book b) {
        return String.format(Locale.ROOT,
                "{\"id\":%d,\"categoria\":\"%s\",\"titulo\":\"%s\"}",
                b.id, escape(opt(b.categoria)), escape(opt(b.titulo)));
    }
    private static String booksToJson(List<Book> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Book b : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append(bookToJson(b));
        }
        sb.append("]");
        return sb.toString();
    }

    // ====== HTTP helpers ======
    private static void addCORS(HttpExchange ex) {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Cache-Control", "no-cache");
        h.add("Date", ZonedDateTime.now().toString());
    }

    private static void sendFile(HttpExchange ex, Path file, String contentType) throws IOException {
        if (!Files.exists(file)) {
            sendText(ex, 404, "Not Found", "text/plain");
            return;
        }
        byte[] bytes = Files.readAllBytes(file);
        Headers h = ex.getResponseHeaders();
        h.add("Content-Type", contentType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void sendText(HttpExchange ex, int code, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Headers h = ex.getResponseHeaders();
        h.add("Content-Type", contentType);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void sendJSON(HttpExchange ex, int code, String json) throws IOException {
        sendText(ex, code, json, "application/json; charset=utf-8");
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".json")) return name.equals("manifest.json")
                ? "application/manifest+json" : "application/json; charset=utf-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static int toInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private static String opt(String s) { return s == null ? "" : s; }
}

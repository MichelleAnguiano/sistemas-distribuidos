import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.*;

public class WeatherService {
    private static final int PORT = 9002;
    private static final Path DATA_FILE = Paths.get("data", "weather.json");
    private static final Map<String, Map<String,Object>> DB = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void main(String[] args) throws Exception {
        ensureDataFile();
        loadFromDisk();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/clima", exchange -> {
            addCORS(exchange);
            try {
                String method = exchange.getRequestMethod();
                if ("OPTIONS".equals(method)) { options(exchange); return; }

                if (!"GET".equals(method)) { sendJSON(exchange, 405, "{\"error\":\"Method not allowed\"}"); return; }

                String path = exchange.getRequestURI().getPath(); // /clima o /clima/CDMX
                if ("/clima".equals(path)) {
                    sendJSON(exchange, 200, toJson(DB));
                } else if (path.startsWith("/clima/")) {
                    String ciudad = path.substring("/clima/".length()).trim();
                    Map<String,Object> w = DB.get(ciudad);
                    if (w != null) sendJSON(exchange, 200, toJson(w));
                    else sendJSON(exchange, 404, "{\"error\":\"Clima no disponible\"}");
                } else {
                    sendJSON(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJSON(exchange, 500, "{\"error\":\"Internal error\"}");
            } finally { exchange.close(); }
        });

        System.out.println("WeatherService en http://127.0.0.1:" + PORT);
        server.start();
    }

    // ====== Utils ======
    private static void ensureDataFile() throws IOException {
        if (!Files.exists(DATA_FILE.getParent())) Files.createDirectories(DATA_FILE.getParent());
        if (!Files.exists(DATA_FILE)) {
            String seed = "{\n" +
                    "  \"CDMX\": {\"ciudad\":\"CDMX\",\"temperatura\": \"22°C\",\"condicion\":\"Soleado\"},\n" +
                    "  \"Guadalajara\": {\"ciudad\":\"Guadalajara\",\"temperatura\":\"24°C\",\"condicion\":\"Parcialmente nublado\"},\n" +
                    "  \"Monterrey\": {\"ciudad\":\"Monterrey\",\"temperatura\":\"26°C\",\"condicion\":\"Caluroso\"}\n" +
                    "}";
            Files.writeString(DATA_FILE, seed, StandardCharsets.UTF_8);
        }
    }

    private static void loadFromDisk() throws IOException {
        String raw = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
        Pattern entry = Pattern.compile("\"(.*?)\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher m = entry.matcher(raw);
        DB.clear();
        while (m.find()) {
            String city = m.group(1);
            String obj = "{" + m.group(2) + "}";
            Map<String,Object> map = parseJsonFlat(obj);
            DB.put(city, map);
        }
    }

    private static Map<String,Object> parseJsonFlat(String json) {
        Map<String,Object> map = new LinkedHashMap<>();
        Matcher ms = Pattern.compile("\"(.*?)\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL).matcher(json);
        while (ms.find()) map.put(ms.group(1), unesc(ms.group(2)));
        Matcher mn = Pattern.compile("\"(.*?)\"\\s*:\\s*(\\d+|true|false)").matcher(json);
        while (mn.find()) map.put(mn.group(1), mn.group(2));
        return map;
    }

    private static String toJson(Map<String,?> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : m.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(esc(e.getKey())).append("\":");
            sb.append(objToJson(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    private static String objToJson(Object v) {
        if (v instanceof Map<?,?> mm) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var e : mm.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(esc(String.valueOf(e.getKey()))).append("\":");
                sb.append(objToJson(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        } else {
            String s = String.valueOf(v);
            if (s.equals("true") || s.equals("false") || s.matches("\\d+")) return s;
            return "\"" + esc(s) + "\"";
        }
    }

    private static String esc(String s){ return s.replace("\\","\\\\").replace("\"","\\\""); }
    private static String unesc(String s){ return s.replace("\\\"","\"").replace("\\\\","\\"); }

    private static void addCORS(HttpExchange ex) {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Headers", "Content-Type");
        h.add("Access-Control-Allow-Methods", "GET,OPTIONS");
        h.add("Cache-Control", "no-store");
        h.add("Date", ZonedDateTime.now().toString());
    }
    private static void options(HttpExchange ex) throws IOException { ex.sendResponseHeaders(204, -1); }
    private static void sendJSON(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}

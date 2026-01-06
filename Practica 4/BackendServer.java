import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.*;

public class BackendServer {
    private final int port;
    private final ExecutorService pool;
    private final Semaphore slots; // sincronización: capacidad M

    public BackendServer(int port, int maxThreads, int maxInService) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(maxThreads);
        this.slots = new Semaphore(maxInService, true);
    }

    public void start() throws IOException {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[BE " + port + "] listo. Threads=" +
                    ((ThreadPoolExecutor)pool).getMaximumPoolSize() + " M=" + slots.availablePermits());
            while (true) {
                Socket s = ss.accept();
                pool.submit(() -> handle(s));
            }
        }
    }

    private void handle(Socket s) {
        String who = s.getRemoteSocketAddress().toString();
        System.out.println("[BE " + port + "] +" + who);
        try (s;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true)) {

            out.println("Backend " + port + " OK. Comandos: ECHO, TIME, MAYUS, SUM. 'exit' para salir.");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("exit")) break;

                // Sección crítica limitada (simula M:30 recursos)
                slots.acquire();
                try {
                    out.println(dispatch(line));
                } finally {
                    slots.release();
                }
            }
        } catch (Exception e) {
            System.err.println("[BE " + port + "] Err: " + e.getMessage());
        } finally {
            System.out.println("[BE " + port + "] -" + who);
        }
    }

    // Multiservicio
    private String dispatch(String line) {
        String[] p = line.trim().split("\\s+");
        if (p.length == 0) return "ERR vacío";
        String cmd = p[0].toUpperCase(Locale.ROOT);
        try {
            switch (cmd) {
                case "ECHO":
                    return joinFrom(p, 1);
                case "TIME":
                    return Instant.now().toString();
                case "MAYUS":
                    return joinFrom(p, 1).toUpperCase(Locale.ROOT);
                case "SUM":
                    if (p.length < 3) return "ERR uso: SUM a b";
                    long a = Long.parseLong(p[1]), b = Long.parseLong(p[2]);
                    return "SUM=" + (a + b);
                default:
                    return "ERR cmd desconocido: " + cmd;
            }
        } catch (Exception e) {
            return "ERR " + e.getMessage();
        }
    }

    private static String joinFrom(String[] p, int i) {
        StringBuilder sb = new StringBuilder();
        for (int k = i; k < p.length; k++) {
            if (k > i) sb.append(' ');
            sb.append(p[k]);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 9001;
        int threads = (args.length > 1) ? Integer.parseInt(args[1]) : 8;
        int M = (args.length > 2) ? Integer.parseInt(args[2]) : 3; // M: capacidad simultánea
        new BackendServer(port, threads, M).start();
    }
}

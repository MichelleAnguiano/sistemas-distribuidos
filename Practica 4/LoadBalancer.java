import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    // Backends registrados (puedes agregar más)
    private static final List<InetSocketAddress> BACKENDS = List.of(
        new InetSocketAddress("127.0.0.1", 9001),
        new InetSocketAddress("127.0.0.1", 9002)
    );
    private static final AtomicInteger rr = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        int port = 8080;
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[LB] Escuchando en " + port + " con " + BACKENDS.size() + " backends...");
            while (true) {
                Socket client = ss.accept();
                new Thread(() -> handle(client)).start();
            }
        }
    }

    private static void handle(Socket client) {
        InetSocketAddress backend = pickBackend();
        try (client;
             Socket srv = new Socket(backend.getHostName(), backend.getPort());
             InputStream cin = client.getInputStream();
             OutputStream cout = client.getOutputStream();
             InputStream sin = srv.getInputStream();
             OutputStream sout = srv.getOutputStream()) {

            // Pipe bidireccional simple
            Thread up = pipe(cin, sout);
            Thread down = pipe(sin, cout);
            up.join();   // cuando el cliente cierra, terminamos
            srv.shutdownOutput();
            down.join();
        } catch (Exception e) {
            System.err.println("[LB] Error: " + e.getMessage());
        }
    }

    private static InetSocketAddress pickBackend() {
        int i = Math.floorMod(rr.getAndIncrement(), BACKENDS.size());
        return BACKENDS.get(i);
    }

    private static Thread pipe(InputStream in, OutputStream out) {
        Thread t = new Thread(() -> {
            try (in; out) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            } catch (IOException ignore) {}
        });
        t.setDaemon(true);
        t.start();
        return t;
    }
}

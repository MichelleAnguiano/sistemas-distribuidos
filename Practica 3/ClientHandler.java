import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RequestQueue sharedQueue;
    private final String clientId;

    public ClientHandler(Socket socket, RequestQueue queue) {
        this.socket = socket;
        this.sharedQueue = queue;
        this.clientId = socket.getRemoteSocketAddress() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    public String getClientId() { return clientId; }

    @Override
    public void run() {
        System.out.println("[+] Cliente conectado: " + clientId);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            out.println("Conectado al servidor. Formato: PRIORIDAD;IMPORTANCIA;MENSAJE");
            out.println("Ejemplo: 1;9;hola");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) break;

                int prio = 2, imp = 5;
                String msg = line;

                try {
                    // Formato: p;i;msg  (todo lo demás se toma como mensaje)
                    String[] parts = line.split(";", 3);
                    if (parts.length >= 2) {
                        prio = Integer.parseInt(parts[0].trim());
                        imp  = Integer.parseInt(parts[1].trim());
                        msg  = parts.length == 3 ? parts[2] : "";
                    }
                } catch (Exception ignore) {}

                Request r = new Request(prio, imp, msg, this);
                sharedQueue.put(r);
                out.println("Recibida -> " + r.toString());
            }
        } catch (IOException e) {
            System.err.println("[" + clientId + "] Error IO: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
            System.out.println("[-] Cliente desconectado: " + clientId);
        }
    }

    /** Lo usa el despachador para responder al cliente. */
    public synchronized void sendResponse(String text) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            out.println(text);
        } catch (IOException e) {
            System.err.println("[" + clientId + "] No se pudo enviar respuesta: " + e.getMessage());
        }
    }
}


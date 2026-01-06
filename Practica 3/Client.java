import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = (args.length > 0) ? args[0] : "127.0.0.1";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            // Mostrar banner del servidor
            for (int i = 0; i < 2; i++) System.out.println(in.readLine());

            System.out.println("Escribe líneas con formato: PRIORIDAD;IMPORTANCIA;MENSAJE");
            System.out.println("Ejemplo: 1;9;hola   |  escribe 'exit' para salir\n");

            // Hilo lector (muestra respuestas asíncronas)
            Thread reader = new Thread(() -> {
                try {
                    String s;
                    while ((s = in.readLine()) != null) {
                        System.out.println("[SRV] " + s);
                    }
                } catch (IOException ignore) {}
            });
            reader.setDaemon(true);
            reader.start();

            // Enviar algunas peticiones demo si no hay teclado (o el usuario prefiere)
            if (System.console() == null) {
                Random rnd = new Random();
                for (int i = 0; i < 5; i++) {
                    int prio = 1 + rnd.nextInt(3);
                    int imp  = 1 + rnd.nextInt(10);
                    out.println(prio + ";" + imp + ";hola-" + i);
                    Thread.sleep(200);
                }
                Thread.sleep(2000);
                out.println("exit");
            } else {
                String line;
                while ((line = stdin.readLine()) != null) {
                    out.println(line);
                    if (line.equalsIgnoreCase("exit")) break;
                }
            }
        }
    }
}

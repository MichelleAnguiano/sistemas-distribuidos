import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMulti {
    public static void main(String[] args) throws Exception {
        String host = (args.length > 0) ? args[0] : "127.0.0.1";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;

        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
             Scanner sc = new Scanner(System.in)) {

            // Mensaje de bienvenida del backend (a través del LB)
            String welcome = in.readLine();
            if (welcome != null) System.out.println("[SRV] " + welcome);

            System.out.println("Comandos: ECHO, TIME, MAYUS, SUM a b   |  'exit' para salir");
            while (true) {
                System.out.print("> ");
                String line = sc.nextLine();
                out.println(line);
                if (line.equalsIgnoreCase("exit")) break;
                String resp = in.readLine();
                System.out.println("[SRV] " + resp);
            }
        }
    }
}

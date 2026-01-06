import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        final String HOST = "localhost"; 
        final int PUERTO = 6000;

        try (
            // 1. socket() y connect() - Crea el socket y se conecta al servidor.
            Socket socket = new Socket(HOST, PUERTO);
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("✅ Conectado al servidor. Escribe 'adios' para salir.");
            String linea;

            do {
                System.out.print("Escribe un mensaje: ");
                linea = scanner.nextLine();

                // 2. write() - Envía el mensaje al servidor.
                salida.println(linea);

                // 3. read() - Lee la respuesta del servidor y la muestra.
                String respuestaServidor = entrada.readLine();
                System.out.println(respuestaServidor);

            } while (!linea.equalsIgnoreCase("adios"));

        } catch (IOException e) {
            System.err.println("❌ Error en el cliente: " + e.getMessage());
        }
    }
}

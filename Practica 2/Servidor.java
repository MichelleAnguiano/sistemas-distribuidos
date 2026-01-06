import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        // Puerto en el que el servidor escuchará.
        final int PUERTO = 6000;

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("✅ Servidor iniciado y escuchando en el puerto " + PUERTO);

            // Bucle infinito para aceptar conexiones de clientes continuamente.
            while (true) {
                System.out.println("Esperando a un cliente...");

                // 1. accept() - Bloquea la ejecución hasta que un cliente se conecte.
                Socket socketCliente = serverSocket.accept();
                System.out.println("¡Cliente conectado desde " + socketCliente.getInetAddress().getHostAddress() + "!");

                // 2. Por cada cliente, crea un nuevo hilo para manejar la comunicación.
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                Thread hiloCliente = new Thread(manejador);
                hiloCliente.start();
            }
        } catch (IOException e) {
            System.err.println("❌ Error en el servidor: " + e.getMessage());
        }
    }
}
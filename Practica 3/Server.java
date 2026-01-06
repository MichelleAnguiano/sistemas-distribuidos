import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int PORT = 8080;

    public static void main(String[] args) {
        RequestQueue queue = new RequestQueue();

        // Iniciar 1 (o más) despachadores que consumen de la cola
        Thread dispatcher = new Thread(new Dispatcher(queue), "dispatcher-1");
        dispatcher.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en puerto " + PORT + " ...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(clientSocket, queue));
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Error en servidor: " + e.getMessage());
        } finally {
            dispatcher.interrupt();
        }
    }
}

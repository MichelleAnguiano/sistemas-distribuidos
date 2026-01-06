import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Runnable permite que esta clase se ejecute en un hilo separado.
public class ManejadorCliente implements Runnable {
    private final Socket socketCliente;

    public ManejadorCliente(Socket socket) {
        this.socketCliente = socket;
    }

    @Override
    public void run() {
        // Usamos try-with-resources para asegurar que todo se cierre automáticamente.
        try (
            PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()))
        ) {
            String mensajeCliente;
            
            // 3. read() - Lee los mensajes que envía el cliente.
            while ((mensajeCliente = entrada.readLine()) != null) {
                System.out.println("Mensaje recibido del cliente: " + mensajeCliente);

                // 4. Lógica de negocio: procesamos el mensaje.
                String respuesta = mensajeCliente.toUpperCase();

                // 5. write() - Enviamos la respuesta de vuelta al cliente.
                salida.println("Respuesta del servidor: " + respuesta);
                
                // Si el cliente envía "adios", terminamos la conexión.
                if (mensajeCliente.equalsIgnoreCase("adios")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error al manejar el cliente: " + e.getMessage());
        } finally {
            try {
                // 6. close() - Cerramos el socket del cliente.
                socketCliente.close();
                System.out.println("Conexión con el cliente cerrada.");
            } catch (IOException e) {
                // Ignorar
            }
        }
    }
}
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HelloServer implements Hello {

    public String sayHello(String name) {
        return "Hola " + name + ", saludo desde el servidor RMI.";
    }

    public static void main(String[] args) {
        try {
            HelloServer obj = new HelloServer();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Crear y registrar el objeto remoto
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("Hello", stub);

            System.out.println("Servidor RMI listo y esperando conexiones...");
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}

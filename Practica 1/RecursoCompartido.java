// RecursoCompartido.java
public class RecursoCompartido {

    private boolean hiloListoParaSerNotificado = false;

    // Método para demostrar el estado BLOCKED
    public synchronized void metodoSincronizado(String nombreHilo) {
        System.out.println("-> " + nombreHilo + " ha entrado al método sincronizado.");
        try {
            Thread.sleep(2000); // Simula trabajo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("<- " + nombreHilo + " está saliendo del método sincronizado.");
    }

    // Método para que un hilo espere de forma segura
    public synchronized void esperar() {
        while (!hiloListoParaSerNotificado) {
            try {
                System.out.println("-> " + Thread.currentThread().getName() + " se prepara para esperar.");
                wait(); // El hilo se pone en WAITING y libera el bloqueo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Una vez que es notificado y la condición es verdadera, el bucle termina.
        System.out.println("<- " + Thread.currentThread().getName() + " ha sido notificado y continúa.");
        hiloListoParaSerNotificado = false;
    }

    // Método para notificar de forma segura
    public synchronized void notificar() {
        System.out.println("📢 " + Thread.currentThread().getName() + " está cambiando la condición y va a notificar.");
        // 1. Cambia la condición (la "memoria" compartida)
        hiloListoParaSerNotificado = true;
        // 2. Notifica a un hilo que esté esperando por este objeto.
        // Si nadie está esperando aún, la bandera 'true' asegurará que no esperen cuando lleguen.
        notify();
    }
}
import java.util.concurrent.Semaphore;

public class Dispatcher implements Runnable {
    private final RequestQueue queue;
    // Recurso crítico simulado con 1 permiso (se podría subir a N para “N servicios”).
    private final Semaphore criticalSection = new Semaphore(1, true);

    public Dispatcher(RequestQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("[*] Dispatcher iniciado");
        while (true) {
            try {
                Request r = queue.take();   // espera bloqueante
                criticalSection.acquire();  // sincronización: un request a la vez en sección crítica

                try {
                    // ----- sección crítica: "procesamiento del servicio" -----
                    System.out.println("[PROC] " + r);
                    // Simulación de trabajo proporcional a la importancia (más imp => más rápido)
                    long workMillis = Math.max(100, 800 - (r.importance * 60L));
                    Thread.sleep(workMillis);

                    String reply = "[OK] Atendido -> prio=" + r.priority +
                                   ", imp=" + r.importance +
                                   ", cliente=" + r.origin.getClientId() +
                                   ", msg=\"" + r.payload + "\"";
                    r.origin.sendResponse(reply);
                } finally {
                    criticalSection.release();
                }
            } catch (InterruptedException ie) {
                System.out.println("[*] Dispatcher detenido.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

// HiloTrabajador.java
public class HiloTrabajador extends Thread {

    private final RecursoCompartido recurso;
    private final boolean esNotificador;

    public HiloTrabajador(String nombre, RecursoCompartido recurso, boolean esNotificador) {
        super(nombre);
        this.recurso = recurso;
        this.esNotificador = esNotificador;
    }

    @Override
    public void run() {
        System.out.println("✅ El hilo " + getName() + " ha iniciado.");

        // 1. Demostración de BLOCKED
        recurso.metodoSincronizado(getName());

        // 2. Demostración de TIMED_WAITING (Dormido)
        try {
            System.out.println("⏳ El hilo " + getName() + " se va a dormir por 1.5 segundos.");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            this.interrupt();
        }

        // 3. Demostración de WAITING / NOTIFY (corregido)
        if (esNotificador) {
            recurso.notificar();
        } else {
            recurso.esperar();
        }

        System.out.println("🏁 El hilo " + getName() + " ha terminado su ejecución.");
    }
}
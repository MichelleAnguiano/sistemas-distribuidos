// SimuladorEstados.java
public class SimuladorEstados {

    public static void main(String[] args) throws InterruptedException {

        RecursoCompartido recurso = new RecursoCompartido();

        // Se crean dos hilos que competirán por el recurso
        HiloTrabajador hilo1 = new HiloTrabajador("Hilo-A (Esperador)", recurso, false);
        HiloTrabajador hilo2 = new HiloTrabajador("Hilo-B (Notificador)", recurso, true);

        // Estado: INICIO (NEW)
        // Los objetos Thread han sido creados pero aún no se han iniciado.
        System.out.println("Estado de " + hilo1.getName() + " antes de iniciar: " + hilo1.getState()); // Debería ser NEW

        // Estado: LISTO (RUNNABLE)
        // Al llamar a start(), los hilos pasan al estado "Listo para ejecutar".
        // El planificador de hilos de la JVM decidirá cuándo pasan a "Ejecución".
        System.out.println("\nIniciando hilos...");
        hilo1.start();
        hilo2.start();

        // Bucle para monitorear los estados de los hilos mientras están vivos
        while (hilo1.isAlive() || hilo2.isAlive()) {
            System.out.println("---------------------------------------------");
            System.out.println("Estado de " + hilo1.getName() + ": " + hilo1.getState());
            System.out.println("Estado de " + hilo2.getName() + ": " + hilo2.getState());
            System.out.println("---------------------------------------------");
            Thread.sleep(1000); // El hilo principal duerme para no saturar la consola
        }

        // Estado: TERMINADO (TERMINATED)
        // Los hilos han completado su método run().
        System.out.println("\nAmbos hilos han terminado.");
        System.out.println("Estado final de " + hilo1.getName() + ": " + hilo1.getState()); // Debería ser TERMINATED
        System.out.println("Estado final de " + hilo2.getName() + ": " + hilo2.getState()); // Debería ser TERMINATED
    }
}
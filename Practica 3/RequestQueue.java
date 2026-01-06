import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class RequestQueue {
    // Regla: primero prioridad (1 mejor), luego mayor importancia, luego tiempo de llegada.
    private final PriorityBlockingQueue<Request> queue = new PriorityBlockingQueue<>(
            100,
            Comparator
                .comparingInt((Request r) -> r.priority)       // menor primero
                .thenComparing((Request r) -> -r.importance)    // mayor primero
                .thenComparingLong(r -> r.arrivalNanos)         // más antiguo primero
                .thenComparingLong(r -> r.seq)                  // rompe empates
    );

    public void put(Request r) { queue.put(r); }
    public Request take() throws InterruptedException { return queue.take(); }
    public int size() { return queue.size(); }
}

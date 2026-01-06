import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Request {
    private static final AtomicLong SEQ = new AtomicLong(0);

    public final long seq;             // rompe empates
    public final long arrivalNanos;    // tiempo de llegada
    public final int priority;         // 1 = alta, 2 = media, 3 = baja
    public final int importance;       // 1..10 (10 muy importante)
    public final String payload;       // mensaje
    public final ClientHandler origin; // para responder

    public Request(int priority, int importance, String payload, ClientHandler origin) {
        this.seq = SEQ.getAndIncrement();
        this.arrivalNanos = System.nanoTime();
        this.priority = priority;
        this.importance = importance;
        this.payload = payload;
        this.origin = origin;
    }

    @Override
    public String toString() {
        return "Request{prio=" + priority + ", imp=" + importance +
               ", at=" + Instant.now() + ", from=" + origin.getClientId() +
               ", msg='" + payload + "'}";
    }
}

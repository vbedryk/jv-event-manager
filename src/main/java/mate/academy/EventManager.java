package mate.academy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventManager {
    private final Set<EventListener> listeners = new CopyOnWriteArraySet<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean isShutdown = false;

    public void registerListener(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener can't be null");
        }
        if (isShutdown) {
            throw new IllegalArgumentException("EventManager is shut down");
        }
        listeners.add(listener);
    }

    public void deregisterListener(EventListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    public void notifyEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (isShutdown) {
            throw new IllegalStateException("EventManager is already shut down");
        }
        List<EventListener> currentListeners = List.copyOf(listeners);
        for (EventListener listener : currentListeners) {
            executorService.submit(() -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.out.println("Error notifying listener: " + e.getMessage());
                }
            });
        }
    }

    public void shutdown() {
        if (isShutdown) {
            return;
        }

        isShutdown = true;
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

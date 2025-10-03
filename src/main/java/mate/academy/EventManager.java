package mate.academy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager {
    private final Set<EventListener> listeners = new CopyOnWriteArraySet<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public void registerListener(EventListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener can't be null");
        }
        if (isShutdown.get()) {
            throw new IllegalStateException("EventManager is shut down");
        }
        listeners.add(listener);
    }

    public void deregisterListener(EventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void notifyEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (isShutdown.get()) {
            throw new IllegalStateException("EventManager is already shut down");
        }

        List<EventListener> currentListeners = List.copyOf(listeners);
        for (EventListener listener : currentListeners) {
            try {
                executorService.submit(() -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.err.println("Task rejected: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        if (!isShutdown.compareAndSet(false, true)) {
            return;
        }

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
        } finally {
            listeners.clear();
        }
    }
}

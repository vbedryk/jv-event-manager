package mate.academy;

import mate.academy.listeners.LoggingListener;
import mate.academy.listeners.SampleListener;

public class Main {
    public static void main(String[] args) {
        EventManager manager = new EventManager();
        EventListener sampleListener = new SampleListener();
        EventListener loggingListener = new LoggingListener();

        manager.registerListener(sampleListener);
        manager.registerListener(loggingListener);

        Event event = new Event("UserLogin", "User123");
        manager.notifyEvent(event);

        manager.deregisterListener(loggingListener);
        Event anotherEvent = new Event("UserLogout", "User123");
        manager.notifyEvent(anotherEvent);

        manager.shutdown();
    }
}

package no.fint.events.internal;

import com.hazelcast.core.HazelcastInstanceNotActiveException;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.FintEventListener;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class EventDispatcher implements Runnable {
    public static final String SYSTEM_TOPIC = "<<SYSTEM>>";
    private final BlockingQueue<Event<?>> queue;
    private final List<FintEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean();

    public EventDispatcher(BlockingQueue<Event<?>> queue, ExecutorService executorService) {
        this.queue = queue;
        this.executorService = executorService;
    }

    public void registerListener(FintEventListener fintEventListener) {
        listeners.add(fintEventListener);
    }

    @Override
    public void run() {
        try {
            if (running.compareAndSet(false, true)) {
                dispatch();
            } else {
                log.debug("Already running");
            }
        } finally {
            running.set(false);
        }
    }

    private void dispatch() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                final Event<?> event = queue.take();
                log.trace("Event received on {}: {}", queue, event);
                listeners.forEach(fintEventListener -> executorService.execute(() -> fintEventListener.accept(event)));
            }
        } catch (HazelcastInstanceNotActiveException | InterruptedException ignore) {
        }
    }

    public void clearListeners() {
        listeners.clear();
    }

    public boolean isRunning() {
        return running.get();
    }
}

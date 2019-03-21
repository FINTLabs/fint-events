package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
import no.fint.events.internal.FintEventsSync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FintEvents {

    private final EventDispatcher downstreamDispatcher;
    private final EventDispatcher upstreamDispatcher;
    private final FintEventsHealth fintEventsHealth;
    private final FintEventsSync fintEventsSync;
    private ExecutorService executorService;


    public FintEvents(EventDispatcher downstreamDispatcher, EventDispatcher upstreamDispatcher, FintEventsHealth fintEventsHealth, FintEventsSync fintEventsSync) {
        this.downstreamDispatcher = downstreamDispatcher;
        this.upstreamDispatcher = upstreamDispatcher;
        this.fintEventsHealth = fintEventsHealth;
        this.fintEventsSync = fintEventsSync;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void registerUpstreamSystemListener(FintEventListener fintEventListener) {
        registerUpstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerDownstreamSystemListener(FintEventListener fintEventListener) {
        registerDownstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerUpstreamListener(String orgId, FintEventListener fintEventListener) {
        upstreamDispatcher.registerListener(orgId, fintEventListener);
        if (!upstreamDispatcher.isRunning()) {
            executorService.execute(upstreamDispatcher);
        }
    }

    public void registerDownstreamListener(String orgId, FintEventListener fintEventListener) {
        downstreamDispatcher.registerListener(orgId, fintEventListener);
        if (!downstreamDispatcher.isRunning()) {
            executorService.execute(downstreamDispatcher);
        }
    }

    public boolean sendUpstream(Event event) {
        return upstreamDispatcher.send(event);
    }

    public boolean sendDownstream(Event event) {
        return downstreamDispatcher.send(event);
    }

    public Event sendHealthCheck(Event event) {
        BlockingQueue<Event> queue = fintEventsHealth.register(event.getCorrId());
        sendDownstream(event);
        try {
            return queue.poll(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Event sendSyncEvent(Event event) {
        BlockingQueue<Event> queue = fintEventsSync.register(event.getCorrId());
        sendDownstream(event);
        try {
            return queue.poll(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearListeners() {
        log.debug("Clearing listeners...");
        downstreamDispatcher.clearListeners();
        upstreamDispatcher.clearListeners();
    }
}

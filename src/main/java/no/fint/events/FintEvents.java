package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FintEvents {

    private final EventDispatcher downstreamDispatcher;
    private final EventDispatcher upstreamDispatcher;
    private final FintEventsHealth fintEventsHealth;


    public FintEvents(EventDispatcher downstreamDispatcher, EventDispatcher upstreamDispatcher, FintEventsHealth fintEventsHealth) {
        this.downstreamDispatcher = downstreamDispatcher;
        this.upstreamDispatcher = upstreamDispatcher;
        this.fintEventsHealth = fintEventsHealth;
    }

    public void registerUpstreamSystemListener(FintEventListener fintEventListener) {
        registerUpstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerDownstreamSystemListener(FintEventListener fintEventListener) {
        registerDownstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerUpstreamListener(String orgId, FintEventListener fintEventListener) {
        upstreamDispatcher.registerListener(orgId, fintEventListener);
    }

    public void registerDownstreamListener(String orgId, FintEventListener fintEventListener) {
        downstreamDispatcher.registerListener(orgId, fintEventListener);
    }

    public void sendUpstream(Event event) {
        upstreamDispatcher.send(event);
    }

    public void sendDownstream(Event event) {
        downstreamDispatcher.send(event);
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

    public void clearListeners() {
        log.debug("Clearing listeners...");
        downstreamDispatcher.clearListeners();
        upstreamDispatcher.clearListeners();
    }
}

package no.fintlabs.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fintlabs.events.internal.EventDispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FintEvents {

    private final EventDispatcher downstreamDispatcher;
    private final EventDispatcher upstreamDispatcher;
    private ExecutorService executorService;


    public FintEvents(EventDispatcher downstreamDispatcher, EventDispatcher upstreamDispatcher) {
        this.downstreamDispatcher = downstreamDispatcher;
        this.upstreamDispatcher = upstreamDispatcher;
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

    public void clearListeners() {
        log.debug("Clearing listeners...");
        downstreamDispatcher.clearListeners();
        upstreamDispatcher.clearListeners();
    }
}

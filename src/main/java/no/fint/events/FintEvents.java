package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FintEvents {

    private final EventDispatcher downstreamDispatcher;
    private final EventDispatcher upstreamDispatcher;
    private final FintEventsHealth fintEventsHealth;


    public FintEvents(@Qualifier("no.fint.events.downstream") EventDispatcher downstreamDispatcher,
                      @Qualifier("no.fint.events.upstream") EventDispatcher upstreamDispatcher,
                      FintEventsHealth fintEventsHealth) {
        this.downstreamDispatcher = downstreamDispatcher;
        this.upstreamDispatcher = upstreamDispatcher;
        this.fintEventsHealth = fintEventsHealth;

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(downstreamDispatcher);
        executorService.execute(upstreamDispatcher);
        log.info("Started event dispatchers");
    }

    public void registerUpstreamListener(String orgId, FintEventListener fintEventListener) {
        upstreamDispatcher.registerListener(orgId, fintEventListener);
    }

    public void registerDownstreamListener(String orgId, FintEventListener fintEventListener) {
        downstreamDispatcher.registerListener(orgId, fintEventListener);
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

    public void clearListeners() {
        downstreamDispatcher.clearListeners();
        upstreamDispatcher.clearListeners();
    }
}

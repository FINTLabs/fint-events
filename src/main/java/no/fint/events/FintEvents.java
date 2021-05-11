package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.QueueService;
import no.fint.events.internal.QueueType;

@Slf4j
public class FintEvents {

    private final QueueService queueService;

    public FintEvents(QueueService queueService) {
        this.queueService = queueService;
    }

    public void registerUpstreamSystemListener(FintEventListener fintEventListener) {
        registerUpstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerDownstreamSystemListener(FintEventListener fintEventListener) {
        registerDownstreamListener(EventDispatcher.SYSTEM_TOPIC, fintEventListener);
    }

    public void registerUpstreamListener(String orgId, FintEventListener fintEventListener) {
        queueService.register(QueueType.UPSTREAM, orgId, fintEventListener);
    }

    public void registerDownstreamListener(String orgId, FintEventListener fintEventListener) {
        queueService.register(QueueType.DOWNSTREAM, orgId, fintEventListener);
    }

    public boolean sendUpstream(Event<?> event) {
        return queueService.send(QueueType.UPSTREAM, event);
    }

    public boolean sendDownstream(Event<?> event) {
        return queueService.send(QueueType.DOWNSTREAM, event);
    }

    public void clearListeners() {
        log.debug("Clearing listeners...");
        queueService.clear();
    }
}

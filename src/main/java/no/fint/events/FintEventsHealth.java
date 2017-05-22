package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.config.FintEventsProps;
import org.redisson.api.RBlockingQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FintEventsHealth {

    @Autowired
    private FintEventsProps props;

    @Autowired
    private FintEvents fintEvents;

    public <T> T sendHealthCheck(String orgId, String id, T value) {
        fintEvents.sendDownstream(orgId, value);

        try {
            RBlockingQueue<T> tempQueue = fintEvents.getTempQueue(id);
            return tempQueue.poll(props.getHealthCheckTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("InterruptedException while waiting for response from health check for id {}, {}", id, e.getMessage());
            return null;
        }
    }

    public <T> void respondHealthCheck(String id, T value) {
        RBlockingQueue<T> tempQueue = fintEvents.getTempQueue(id);
        tempQueue.offer(value);
    }

}

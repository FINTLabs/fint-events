package no.fint.events.testutils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.event.RedissonReconnectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestReconnectedEventListener {

    @Getter
    private boolean reconnected = false;

    @EventListener(RedissonReconnectedEvent.class)
    public void reconnect() {
        log.info("RedissonReconnectedEvent received");
        reconnected = true;
    }
}

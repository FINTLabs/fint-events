package no.fint.events.config;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedissonConnection {

    @Autowired
    private FintEvents fintEvents;

    @Scheduled(initialDelay = 20000L, fixedDelayString = ("${" + RedissonConfig.REDISSON_PING_INTERVAL + "}"))
    public void checkRedisConnection() {
        if (connectionLost()) {
            log.warn("Lost connection to redis, trying to reconnect.");
            fintEvents.reconnect();
        }
    }

    boolean connectionLost() {
        try {
            return !(fintEvents.getClient().getNodesGroup().pingAll());
        } catch (RedisException e) {
            log.warn("Exception when executing redis ping, {}", e.getMessage());
            return true;
        }
    }

}

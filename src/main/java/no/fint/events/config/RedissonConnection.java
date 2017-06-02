package no.fint.events.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class RedissonConnection {

    @Autowired
    private RedissonConfig redissonConfig;

    @Autowired
    private FintEvents fintEvents;

    @Getter(AccessLevel.PACKAGE)
    private AtomicBoolean disconnected = new AtomicBoolean(false);

    @Scheduled(initialDelay = 20000L, fixedDelayString = ("${" + RedissonConfig.REDISSON_PING_INTERVAL + "}"))
    public void checkRedisConnection() {
        if (Boolean.valueOf(redissonConfig.getAutoReconnect())) {
            if (connectionLost()) {
                log.warn("Lost connection to redis, waiting for server to come back online");
                disconnected.set(true);
            } else if (disconnected.get()) {
                log.info("Redis instance back, trying to reconnect");
                disconnected.set(false);
                fintEvents.reconnect();
            }
        }
    }

    boolean connectionLost() {
        try {
            return !(fintEvents.getClient().getNodesGroup().pingAll());
        } catch (RedisException | RejectedExecutionException e) {
            log.warn("Exception when running redis ping, {}", e.getMessage());
            return true;
        }
    }

}

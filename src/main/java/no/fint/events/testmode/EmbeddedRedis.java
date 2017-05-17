package no.fint.events.testmode;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.config.FintEventsProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@Component
public class EmbeddedRedis {
    private RedisServer redisServer = null;

    @Autowired
    private FintEventsProps props;

    @PostConstruct
    public void init() throws IOException {
        if (Boolean.valueOf(props.getTestMode()) && redisServer == null) {
            log.info("Test mode enabled, starting embedded redis");
            redisServer = new RedisServer(6379);
            try {
                redisServer.start();
            } catch (RuntimeException e) {
                throw new RuntimeException("Could not start embedded redis, is there already an instance running on prt 6379? " + e.getMessage());
            }
        }
    }

    public boolean isStarted() {
        return redisServer != null && redisServer.isActive();
    }

    @PreDestroy
    public void shutdown() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}

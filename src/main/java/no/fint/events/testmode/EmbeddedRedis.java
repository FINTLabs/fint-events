package no.fint.events.testmode;

import no.fint.events.config.FintEventsProps;
import org.springframework.beans.factory.annotation.Autowired;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

public class EmbeddedRedis {
    private RedisServer redisServer = null;

    @Autowired
    private FintEventsProps props;

    @PostConstruct
    public void init() throws IOException {
        if (Boolean.valueOf(props.getTestMode())) {
            redisServer = new RedisServer(6379);
            redisServer.start();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}

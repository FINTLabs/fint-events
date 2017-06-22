package no.fint.events.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Component
public class FintEventsProps {

    public static final String QUEUE_ENDPOINT_ENABLED = "fint.events.queue-endpoint-enabled";

    @Autowired
    private RedissonConfig redisson;

    @Getter
    @Value("${fint.events.orgIds:}")
    private String[] orgIds;

    @Getter
    @Value("${fint.events.env:local}")
    private String env;

    @Getter
    @Value("${fint.events.component:default}")
    private String component;

    @Getter
    @Value("${fint.events.default-downstream-queue:downstream_{component}_{env}_{orgId}}")
    private String defaultDownstreamQueue;

    @Getter
    @Value("${fint.events.default-upstream-queue:upstream_{component}_{env}_{orgId}}")
    private String defaultUpstreamQueue;

    @Getter
    @Value("${fint.events.test-mode:false}")
    private String testMode;

    @Getter
    @Value("${fint.events.test-mode.docker-redis:true}")
    private String dockerRedis;

    @Value("${" + QUEUE_ENDPOINT_ENABLED + ":false}")
    private String queueEndpointEnabled;

    @Getter
    @Value("${fint.events.healthcheck.timeout-in-seconds:90}")
    private int healthCheckTimeout;

    @Getter
    private Config redissonConfig;

    @PostConstruct
    public void init() throws IOException {
        log.info("Started with env:{}, component:{}", env, component);

        if (Boolean.valueOf(testMode)) {
            log.info("Test-mode enabled, loading default redisson config");
            redissonConfig = redisson.getDefaultConfig();
        } else {
            redissonConfig = redisson.getConfig();
        }

        if (Boolean.valueOf(queueEndpointEnabled)) {
            log.info("Queue endpoint enabled, initializing FintEventsController");
        } else {
            log.info("Queue endpoint disabled, will not load FintEventsController");
        }
    }

}

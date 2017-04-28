package no.fint.events.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class FintEventsProps {

    @Value("${fint.events.redis-configuration:" + RedisConfiguration.SINGLE + "}")
    private String redisConfiguration;

    @Value("${fint.events.redis-address:localhost:6379}")
    private String redisAddress;

    @Value("${fint.events.default-downstream-queue:%s.downstream}")
    private String defaultDownstreamQueue;

    @Value("${fint.events.default-upstream-queue:%s.upstream}")
    private String defaultUpstreamQueue;

    @Value("${fint.events.test-mode:false}")
    private String testMode;

}

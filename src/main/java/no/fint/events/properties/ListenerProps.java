package no.fint.events.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Getter
public class ListenerProps {

    @Value("${fint.listener.retry.initial-interval:30000}")
    private int retryInitialInterval;

    @Value("${fint.listener.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${fint.listener.retry.max-interval:30000}")
    private int retryMaxInterval;

    @Value("${fint.listener.retry.multiplier:1.0}")
    private double retryMultiplier;

    @Value("${fint.deadletter.exchange:deadletter.exchange}")
    private String deadletterExchange;

    @Value("${fint.deadletter.queue:deadletter.queue}")
    private String deadletterQueue;

    @Value("${fint.listener.acknowledge-mode:AUTO}")
    private String acknowledgeMode;
}

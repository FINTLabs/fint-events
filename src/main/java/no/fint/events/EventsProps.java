package no.fint.events;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class EventsProps {

    @Value("${fint.events.orgs:hfk.no, vaf.no, rogfk.no}")
    private String[] organisations;

    @Value("${fint.events.default-input-queue:%s.input}")
    private String defaultInputQueue;

    @Value("${fint.events.default-output-queue:%s.output}")
    private String defaultOutputQueue;

    @Value("${fint.events.default-error-queue:%s.error}")
    private String defaultErrorQueue;


    @Value("${spring.rabbitmq.listener.retry.initial-interval:1000}")
    private int retryInitialInterval;

    @Value("${spring.rabbitmq.listener.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${spring.rabbitmq.listener.retry.max-interval:10000}")
    private int retryMaxInterval;

    @Value("${spring.rabbitmq.listener.retry.multiplier:1.0}")
    private double retryMultiplier;

    @Value("${spring.rabbitmq.deadletter.exchange:deadletter.exchange}")
    private String deadletterExchange;

    @Value("${spring.rabbitmq.deadletter.queue:deadletter.queue}")
    private String deadletterQueue;
}

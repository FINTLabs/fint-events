package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FintEvents {

    @Autowired
    private Environment environment;

    @Autowired
    private Events events;

    @Autowired
    private EventsProps eventsProps;

    private List<Organization> organizations;

    @PostConstruct
    public void init() {
        organizations = getDefaultQueues();
        if (environment.acceptsProfiles("!norabbitmq")) {
            organizations.forEach(organization -> {
                log.info("Setting up queue for: {}", organization.getName());
                events.addQueues(organization.getExchange(),
                        organization.getInputQueue(),
                        organization.getOutputQueue(),
                        organization.getErrorQueue());
            });
        }
    }

    public void deleteDefaultQueues() {
        getDefaultQueues().forEach(organization -> events.deleteQueues(
                organization.getExchange(),
                organization.getInputQueue(),
                organization.getOutputQueue(),
                organization.getErrorQueue()));
    }

    List<Organization> getDefaultQueues() {
        return Arrays.stream(eventsProps.getOrganisations()).map(org -> new Organization(
                org,
                eventsProps.getDefaultInputQueue(),
                eventsProps.getDefaultOutputQueue(),
                eventsProps.getDefaultErrorQueue()))
                .collect(Collectors.toList());
    }

    public void registerInputListener(Class<?> listener) {
        registerListener(listener, EventType.INPUT);
    }

    public void registerOutputListener(Class<?> listener) {
        registerListener(listener, EventType.OUTPUT);
    }

    public void registerErrorListener(Class<?> listener) {
        registerListener(listener, EventType.ERROR);
    }

    private void registerListener(Class<?> listener, EventType eventType) {
        organizations.forEach(org -> {
            Queue queue = org.getQueue(eventType);
            Optional<SimpleMessageListenerContainer> listenerContainer = events.registerListener(org.getExchange(), queue, listener);
            if (listenerContainer.isPresent()) {
                addRetry(org, listenerContainer.get());
            } else {
                log.error("Unable to register retry interceptor for {}", org.getName());
            }
        });
    }

    private void addRetry(Organization org, SimpleMessageListenerContainer listenerContainer) {
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(eventsProps.getRetryMaxAttempts())
                .backOffOptions(eventsProps.getRetryInitialInterval(), eventsProps.getRetryMultiplier(), eventsProps.getRetryMaxInterval())
                .recoverer(new RepublishMessageRecoverer(events.rabbitTemplate(), org.getExchangeName(), org.getErrorQueueName()))
                .build();
        listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
    }

}

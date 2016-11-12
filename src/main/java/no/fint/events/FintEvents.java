package no.fint.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import javax.annotation.PostConstruct;
import java.io.IOException;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    public void addOrganization(String orgId) {
        Organization organization = new Organization(
                orgId,
                eventsProps.getDefaultInputQueue(),
                eventsProps.getDefaultOutputQueue(),
                eventsProps.getDefaultErrorQueue()
        );

        organizations.add(organization);
        events.addQueues(
                organization.getExchange(),
                organization.getInputQueue(),
                organization.getOutputQueue(),
                organization.getErrorQueue()
        );
    }

    public void removeOrganization(String orgId) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            Organization org = organization.get();
            events.deleteQueues(
                    org.getExchange(),
                    org.getInputQueue(),
                    org.getOutputQueue(),
                    org.getErrorQueue()
            );

            Optional<Integer> index = getOrganizationIndex(orgId);
            if (index.isPresent()) {
                organizations.remove(index.get().intValue());
            }
        } else {
            log.error("Organization {} not found", orgId);
        }
    }

    public List<String> getRegisteredOrgIds() {
        return organizations.stream().map(Organization::getName).collect(Collectors.toList());
    }

    public boolean containsOrganization(String orgId) {
        return getOrganization(orgId).isPresent();
    }

    public void deleteDefaultQueues() {
        getDefaultQueues().forEach(organization -> events.deleteQueues(
                organization.getExchange(),
                organization.getInputQueue(),
                organization.getOutputQueue(),
                organization.getErrorQueue()));
    }

    List<Organization> getDefaultQueues() {
        return Arrays.stream(eventsProps.getOrganizations()).map(org -> new Organization(
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

    public Optional<Message> readErrorMessage(String orgId) {
        return readOrganizationMessage(EventType.ERROR, orgId);
    }

    public <T> Optional<T> readErrorJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.ERROR, orgId, responseType);
    }

    public Optional<Message> readOutputMessage(String orgId) {
        return readOrganizationMessage(EventType.OUTPUT, orgId);
    }

    public <T> Optional<T> readOutputJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.OUTPUT, orgId, responseType);
    }

    public Optional<Message> readInputMessage(String orgId) {
        return readOrganizationMessage(EventType.INPUT, orgId);
    }

    public <T> Optional<T> readInputJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.INPUT, orgId, responseType);
    }

    private <T> Optional<T> readOrganizationJson(EventType type, String orgId, Class<T> responseType) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            Queue queue = organization.get().getQueue(type);
            return readJson(queue.getName(), responseType);
        } else {
            throw new IllegalArgumentException("No organization with id " + orgId);
        }
    }

    private Optional<Message> readOrganizationMessage(EventType type, String orgId) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            Queue queue = organization.get().getQueue(type);
            return Optional.ofNullable(readMessage(queue.getName()));
        } else {
            throw new IllegalArgumentException("No organization with id " + orgId);
        }
    }

    private Optional<Organization> getOrganization(String orgId) {
        return organizations.stream().filter(org -> org.getName().equals(orgId)).findAny();
    }

    private Optional<Integer> getOrganizationIndex(String orgId) {
        for (int i = 0; i < organizations.size(); i++) {
            Organization o = organizations.get(i);
            if (o.getName().equals(orgId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public <T> Optional<T> readJson(String queue, Class<T> responseType) {
        try {
            Message message = readMessage(queue);
            if (message == null) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(objectMapper.readValue(message.getBody(), responseType));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create object from json message", e);
        }
    }

    public Message readMessage(String queue) {
        RabbitTemplate rabbitTemplate = events.rabbitTemplate(queue);
        return rabbitTemplate.receive();
    }

}

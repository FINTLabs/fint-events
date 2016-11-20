package no.fint.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Events events;

    @Autowired
    private EventsProps eventsProps;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Organization> organizations;

    @PostConstruct
    public void init() {
        organizations = getDefaultQueues();
        String testMode = eventsProps.getTestMode();
        if (!Boolean.valueOf(testMode)) {
            organizations.forEach(organization -> {
                log.info("Setting up queue for: {}", organization.getName());
                events.addQueues(organization.getExchange(),
                        organization.getDownstreamQueue(),
                        organization.getUpstreamQueue(),
                        organization.getErrorQueue());
            });
        }
    }

    public void addOrganization(String orgId) {
        Organization organization = new Organization(
                orgId,
                eventsProps.getDefaultDownstreamQueue(),
                eventsProps.getDefaultUpstreamQueue(),
                eventsProps.getDefaultErrorQueue()
        );

        organizations.add(organization);
        events.addQueues(
                organization.getExchange(),
                organization.getDownstreamQueue(),
                organization.getUpstreamQueue(),
                organization.getErrorQueue()
        );
    }

    public void removeOrganization(String orgId) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            Organization org = organization.get();
            events.deleteQueues(
                    org.getExchange(),
                    org.getDownstreamQueue(),
                    org.getUpstreamQueue(),
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
                organization.getDownstreamQueue(),
                organization.getUpstreamQueue(),
                organization.getErrorQueue()));
    }

    List<Organization> getDefaultQueues() {
        return Arrays.stream(eventsProps.getOrganizations()).map(org -> new Organization(
                org,
                eventsProps.getDefaultDownstreamQueue(),
                eventsProps.getDefaultUpstreamQueue(),
                eventsProps.getDefaultErrorQueue()))
                .collect(Collectors.toList());
    }

    public void registerDownstreamListener(String orgId, Class<?> listener) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            registerListener(listener, EventType.DOWNSTREAM, organization.get());
        }
    }

    public void registerDownstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.DOWNSTREAM);
    }

    public void registerUpstreamListener(String orgId, Class<?> listener) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            registerListener(listener, EventType.UPSTREAM, organization.get());
        }
    }

    public void registerUpstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.UPSTREAM);
    }

    public void registerErrorListener(String orgId, Class<?> listener) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            registerListener(listener, EventType.ERROR, organization.get());
        }
    }

    public void registerErrorListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.ERROR);
    }

    private void registerOrganizationListeners(Class<?> listener, EventType eventType) {
        organizations.forEach(org -> registerListener(listener, eventType, org));
    }

    private void registerListener(Class<?> listener, EventType eventType, Organization org) {
        Queue queue = org.getQueue(eventType);
        Optional<SimpleMessageListenerContainer> listenerContainer = events.registerListener(org.getExchange(), queue, listener);
        if (listenerContainer.isPresent()) {
            addRetry(org, listenerContainer.get());
            addAcknowledgeMode(listenerContainer.get());
        } else {
            log.error("Unable to register retry interceptor for {}", org.getName());
        }
    }

    private void addRetry(Organization org, SimpleMessageListenerContainer listenerContainer) {
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(eventsProps.getRetryMaxAttempts())
                .backOffOptions(eventsProps.getRetryInitialInterval(), eventsProps.getRetryMultiplier(), eventsProps.getRetryMaxInterval())
                .recoverer(new RepublishMessageRecoverer(events.rabbitTemplate(), org.getExchangeName(), org.getErrorQueueName()))
                .build();
        listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
    }

    private void addAcknowledgeMode(SimpleMessageListenerContainer listenerContainer) {
        String acknowledgeModeProp = eventsProps.getAcknowledgeMode();
        AcknowledgeMode acknowledgeMode = AcknowledgeMode.valueOf(acknowledgeModeProp);
        listenerContainer.setAcknowledgeMode(acknowledgeMode);
    }

    public Optional<Message> readErrorMessage(String orgId) {
        return readOrganizationMessage(EventType.ERROR, orgId);
    }

    public <T> Optional<T> readErrorJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.ERROR, orgId, responseType);
    }

    public Optional<Message> readUpstreamMessage(String orgId) {
        return readOrganizationMessage(EventType.UPSTREAM, orgId);
    }

    public <T> Optional<T> readUpstreamJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.UPSTREAM, orgId, responseType);
    }

    public Optional<Message> readDownstreamMessage(String orgId) {
        return readOrganizationMessage(EventType.DOWNSTREAM, orgId);
    }

    public <T> Optional<T> readDownstreamJson(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.DOWNSTREAM, orgId, responseType);
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

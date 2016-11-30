package no.fint.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.properties.EventsProps;
import no.fint.events.properties.ListenerProps;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FintEvents {

    @Autowired
    private Events events;

    @Autowired
    private EventsProps eventsProps;

    @Autowired
    private ListenerProps listenerProps;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Organization> organizations;

    @PostConstruct
    public void init() {
        organizations = getDefaultQueues();
        if (!eventsProps.isTestMode()) {
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
            index.ifPresent(integer -> organizations.remove(integer.intValue()));
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

    public <T> Optional<T> sendAndReceiveObject(String orgId, String id, Object message, Class<T> type) {
        Optional<Organization> org = getOrganization(orgId);
        if (org.isPresent()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                Organization organization = org.get();
                Message response = events.sendAndReceive(organization.getExchangeName(), organization.getDownstreamQueueName(), id, json);
                return Optional.ofNullable(objectMapper.readValue(response.getBody(), type));
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read/write object from json", e);
            }
        } else {
            return Optional.empty();
        }
    }

    public void sendDownstreamMessage(String orgId, String message) {
        getOrganization(orgId).ifPresent(organization -> events.send(organization.getDownstreamQueueName(), message));
    }

    public void sendDownstreamObject(String orgId, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            sendDownstreamMessage(orgId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to create json from object", e);
        }
    }

    public void sendUpstreamMessage(String orgId, String message) {
        getOrganization(orgId).ifPresent(organization -> events.send(organization.getUpstreamQueueName(), message));
    }

    public void sendUpstreamObject(String orgId, String corrId, Object message) {
        try {
            Optional<Organization> organization = getOrganization(orgId);
            if (organization.isPresent()) {
                String json = objectMapper.writeValueAsString(message);
                String queueName = organization.get().getUpstreamQueueName() + "." + corrId;

                Map<String, Object> arguments = new HashMap<>();
                arguments.put("x-message-ttl", 30000);
                arguments.put("x-expires", 35000);
                Queue queue = new Queue(queueName, false, false, true, arguments);
                events.addQueues(new TopicExchange(orgId), queue);
                events.send(queueName, json);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to create json from object", e);
        }
    }

    public void sendUpstreamObject(String orgId, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            sendUpstreamMessage(orgId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to create json from object", e);
        }
    }

    public void sendErrorMessage(String orgId, String message) {
        getOrganization(orgId).ifPresent(organization -> events.send(organization.getErrorQueueName(), message));
    }

    public void sendErrorObject(String orgId, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            sendErrorMessage(orgId, json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to create json from object", e);
        }
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
        getOrganization(orgId).ifPresent(organization -> registerListener(listener, EventType.DOWNSTREAM, organization));
    }

    public void registerDownstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.DOWNSTREAM);
    }

    public void registerUpstreamListener(String orgId, Class<?> listener) {
        Optional<Organization> organization = getOrganization(orgId);
        organization.ifPresent(org -> registerListener(listener, EventType.UPSTREAM, org));
    }

    public void registerUpstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.UPSTREAM);
    }

    public void registerErrorListener(String orgId, Class<?> listener) {
        Optional<Organization> organization = getOrganization(orgId);
        organization.ifPresent(org -> registerListener(listener, EventType.ERROR, org));
    }

    public void registerErrorListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.ERROR);
    }

    private void registerOrganizationListeners(Class<?> listener, EventType eventType) {
        organizations.forEach(org -> registerListener(listener, eventType, org));
    }

    private void registerListener(Class<?> listener, EventType eventType, Organization org) {
        Queue queue = org.getQueue(eventType);
        Optional<SimpleMessageListenerContainer> listenerContainer = events.registerUnstartedListener(org.getExchange(), queue, listener);
        if (listenerContainer.isPresent()) {
            addRetry(org, listenerContainer.get());
            addAcknowledgeMode(listenerContainer.get());
            listenerContainer.get().start();
        }
    }

    private void addRetry(Organization org, SimpleMessageListenerContainer listenerContainer) {
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(listenerProps.getRetryMaxAttempts())
                .backOffOptions(listenerProps.getRetryInitialInterval(), listenerProps.getRetryMultiplier(), listenerProps.getRetryMaxInterval())
                .recoverer(new RepublishMessageRecoverer(events.rabbitTemplate(), org.getExchangeName(), org.getErrorQueueName()))
                .build();
        listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
    }

    private void addAcknowledgeMode(SimpleMessageListenerContainer listenerContainer) {
        String acknowledgeModeProp = listenerProps.getAcknowledgeMode();
        AcknowledgeMode acknowledgeMode = AcknowledgeMode.valueOf(acknowledgeModeProp);
        listenerContainer.setAcknowledgeMode(acknowledgeMode);
    }

    public Optional<Message> readErrorMessage(String orgId) {
        return readOrganizationMessage(EventType.ERROR, orgId);
    }

    public <T> Optional<T> readErrorObject(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.ERROR, orgId, responseType);
    }

    public Optional<Message> readUpstreamMessage(String orgId) {
        return readOrganizationMessage(EventType.UPSTREAM, orgId);
    }

    public <T> Optional<T> readUpstreamObject(String orgId, String corrId, Class<T> responseType) {
        Optional<Organization> organization = getOrganization(orgId);
        if (organization.isPresent()) {
            return readJson(organization.get().getUpstreamQueueName() + "." + corrId, responseType);
        } else {
            throw new IllegalArgumentException("No organization with id " + orgId);
        }
    }

    public <T> Optional<T> readUpstreamObject(String orgId, Class<T> responseType) {
        return readOrganizationJson(EventType.UPSTREAM, orgId, responseType);
    }

    public Optional<Message> readDownstreamMessage(String orgId) {
        return readOrganizationMessage(EventType.DOWNSTREAM, orgId);
    }

    public <T> Optional<T> readDownstreamObject(String orgId, Class<T> responseType) {
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

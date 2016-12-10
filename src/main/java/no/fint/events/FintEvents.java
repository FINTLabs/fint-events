package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.fintevents.EventType;
import no.fint.events.fintevents.FintListeners;
import no.fint.events.fintevents.FintOrganisation;
import no.fint.events.fintevents.FintOrganisations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public class FintEvents {

    @Autowired
    private Events events;

    @Autowired
    private FintOrganisations organisations;

    @Autowired
    private FintListeners listeners;

    private Class<?> defaultType;

    public void setDefaultType(Class<?> defaultType) {
        this.defaultType = defaultType;
    }

    public <T> Optional<T> sendAndReceiveDownstream(String orgId, Object message, Class<T> type) {
        return sendAndReceive(EventType.DOWNSTREAM, orgId, message, type);
    }

    public void sendDownstream(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getDownstreamQueueName(), message, type));
    }

    public void sendDownstream(String orgId, Object message) {
        verifyDefaultType();
        sendDownstream(orgId, message, defaultType);
    }

    public <T> Optional<T> sendAndReceiveUpstream(String orgId, Object message, Class<T> type) {
        return sendAndReceive(EventType.UPSTREAM, orgId, message, type);
    }

    public void sendUpstream(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getUpstreamQueueName(), message, type));
    }

    public void sendUpstream(String orgId, Object message) {
        verifyDefaultType();
        sendUpstream(orgId, message, defaultType);
    }

    public void sendUndelivered(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getUndeliveredQueueName(), message, type));
    }

    public void sendUndelivered(String orgId, Object message) {
        verifyDefaultType();
        sendUndelivered(orgId, message, defaultType);
    }

    public void registerDownstreamListener(String orgId, Class<?> listener) {
        organisations.get(orgId).ifPresent(org -> listeners.register(listener, EventType.DOWNSTREAM, org));
    }

    public void registerDownstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.DOWNSTREAM);
    }

    public void registerUpstreamListener(String orgId, Class<?> listener) {
        organisations.get(orgId).ifPresent(org -> listeners.register(listener, EventType.UPSTREAM, org));
    }

    public void registerUpstreamListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.UPSTREAM);
    }

    public void registerUndeliveredListener(String orgId, Class<?> listener) {
        organisations.get(orgId).ifPresent(org -> listeners.register(listener, EventType.UNDELIVERED, org));
    }

    public void registerUndeliveredListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.UNDELIVERED);
    }

    private void registerOrganizationListeners(Class<?> listener, EventType eventType) {
        organisations.getAll().forEach(org -> listeners.register(listener, eventType, org));
    }

    public <T> Optional<T> readUndelivered(String orgId, Class<T> type) {
        return read(EventType.UNDELIVERED, orgId, type);
    }

    public <T> Optional<T> readUpstream(String orgId, Class<T> responseType) {
        return read(EventType.UPSTREAM, orgId, responseType);
    }

    public <T> Optional<T> readDownstream(String orgId, Class<T> responseType) {
        return read(EventType.DOWNSTREAM, orgId, responseType);
    }

    private <T> Optional<T> sendAndReceive(EventType type, String orgId, Object message, Class<T> messageType) {
        Optional<FintOrganisation> org = organisations.get(orgId);
        if (org.isPresent()) {
            T response = events.sendAndReceive(org.get().getExchangeName(), org.get().getQueue(type).getName(), message, messageType);
            return Optional.ofNullable(response);
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> read(EventType type, String orgId, Class<T> responseType) {
        Optional<FintOrganisation> org = organisations.get(orgId);
        if (org.isPresent()) {
            RabbitTemplate rabbitTemplate = events.rabbitTemplate(responseType);
            Object value = rabbitTemplate.receiveAndConvert(org.get().getQueue(type).getName());
            return Optional.ofNullable((T) value);
        }

        return Optional.empty();
    }

    private void verifyDefaultType() {
        if (defaultType == null) {
            throw new IllegalStateException("No default type set");
        }
    }

}

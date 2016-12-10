package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.fintevents.FintOrganisations;
import no.fint.events.fintevents.FintListeners;
import no.fint.events.fintevents.FintOrganisation;
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

    public <T> Optional<T> sendAndReceive(String orgId, Object message, Class<T> type) {
        Optional<FintOrganisation> org = organisations.get(orgId);
        if (org.isPresent()) {
            FintOrganisation organisation = org.get();
            T response = events.sendAndReceive(organisation.getExchangeName(), organisation.getDownstreamQueueName(), message, type);
            return Optional.ofNullable(response);
        } else {
            return Optional.empty();
        }
    }

    public void sendDownstream(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getDownstreamQueueName(), message, type));
    }

    public void sendDownstream(String orgId, Object message) {
        verifyDefaultType();
        sendDownstream(orgId, message, defaultType);
    }

    public void sendUpstream(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getUpstreamQueueName(), message, type));
    }

    public void sendUpstream(String orgId, Object message) {
        verifyDefaultType();
        sendUpstream(orgId, message, defaultType);
    }

    public void sendError(String orgId, Object message, Class<?> type) {
        organisations.get(orgId).ifPresent(org -> events.send(org.getErrorQueueName(), message, type));
    }

    public void sendError(String orgId, Object message) {
        verifyDefaultType();
        sendError(orgId, message, defaultType);
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

    public void registerErrorListener(String orgId, Class<?> listener) {
        organisations.get(orgId).ifPresent(org -> listeners.register(listener, EventType.ERROR, org));
    }

    public void registerErrorListener(Class<?> listener) {
        registerOrganizationListeners(listener, EventType.ERROR);
    }

    private void registerOrganizationListeners(Class<?> listener, EventType eventType) {
        organisations.getAll().forEach(org -> listeners.register(listener, eventType, org));
    }

    public <T> Optional<T> readError(String orgId, Class<T> type) {
        return readOrganisationObject(EventType.ERROR, orgId, type);
    }

    public <T> Optional<T> readUpstream(String orgId, Class<T> responseType) {
        return readOrganisationObject(EventType.UPSTREAM, orgId, responseType);
    }

    public <T> Optional<T> readDownstream(String orgId, Class<T> responseType) {
        return readOrganisationObject(EventType.DOWNSTREAM, orgId, responseType);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> readOrganisationObject(EventType type, String orgId, Class<T> responseType) {
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

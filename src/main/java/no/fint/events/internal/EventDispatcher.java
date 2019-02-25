package no.fint.events.internal;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.FintEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class EventDispatcher implements MessageListener<Event> {
    public static final String SYSTEM_TOPIC = "<<SYSTEM>>";
    private final Map<String, FintEventListener> listeners = new HashMap<>();
    private final ExecutorService executorService;
    private final ITopic<Event> topic;

    public EventDispatcher(ITopic<Event> topic) {
        this.topic = topic;
        this.topic.addMessageListener(this);
        this.executorService = Executors.newCachedThreadPool();
    }

    @Synchronized
    public void registerListener(String orgId, FintEventListener fintEventListener) {
        listeners.put(orgId, fintEventListener);
    }

    public void send(Event event) {
        topic.publish(event);
    }

    @Synchronized
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public void onMessage(Message<Event> message) {
        final Event event = message.getMessageObject();
        log.trace("Event received: {}", event);
        String orgId = event.getOrgId();
        if (event.isRegisterOrgId()) {
            orgId = SYSTEM_TOPIC;
        }
        FintEventListener fintEventListener = listeners.get(orgId);
        if (fintEventListener == null) {
            log.error("No listener found for orgId: {}", orgId);
        } else {
            executorService.execute(() -> fintEventListener.accept(event));
        }
    }
}

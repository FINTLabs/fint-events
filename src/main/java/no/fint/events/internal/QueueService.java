package no.fint.events.internal;

import com.hazelcast.core.HazelcastInstance;
import no.fint.event.model.Event;
import no.fint.events.FintEventListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static no.fint.events.internal.EventDispatcher.SYSTEM_TOPIC;

public class QueueService {
    private final HazelcastInstance hazelcastInstance;
    private final ConcurrentMap<String, EventDispatcher> dispatchers = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public QueueService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        executorService = Executors.newCachedThreadPool();
    }

    public boolean send(QueueType queueType, Event event) {
        final EventDispatcher dispatcher = getDispatcherFor(queueType, getTopic(event));
        if (!dispatcher.isRunning()) {
            executorService.execute(dispatcher);
        }
        return dispatcher.send(event);
    }

    private String getTopic(Event event) {
        if (event.isRegisterOrgId()) {
            return SYSTEM_TOPIC;
        }
        return event.getOrgId();
    }

    public void register(QueueType queueType, String orgId, FintEventListener listener) {
        final EventDispatcher dispatcher = getDispatcherFor(queueType, orgId);
        dispatcher.registerListener(orgId, listener);
    }

    private EventDispatcher getDispatcherFor(QueueType queueType, String orgId) {
        return dispatchers.computeIfAbsent(queueType.getQueueName(orgId), this::createDispatcher);
    }

    private EventDispatcher createDispatcher(String queueName) {
        return new EventDispatcher(hazelcastInstance.getQueue(queueName), executorService);
    }

    public void clear() {
        dispatchers.values().forEach(EventDispatcher::clearListeners);
        dispatchers.clear();
    }
}

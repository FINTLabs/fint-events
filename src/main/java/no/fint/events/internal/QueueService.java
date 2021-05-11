package no.fint.events.internal;

import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.FintEventListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import static no.fint.events.internal.EventDispatcher.SYSTEM_TOPIC;

@Slf4j
public class QueueService {
    private final HazelcastInstance hazelcastInstance;
    private final ExecutorService executorService;
    private final ConcurrentMap<String, EventDispatcher> dispatchers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, BlockingQueue<Event<?>>> queues = new ConcurrentHashMap<>();

    public QueueService(HazelcastInstance hazelcastInstance, ExecutorService executorService) {
        this.hazelcastInstance = hazelcastInstance;
        this.executorService = executorService;
    }

    public boolean send(QueueType queueType, Event<?> event) {
        final String topic = getTopic(event);
        log.debug("Send {} {}", queueType, topic);
        log.trace("Send event {}", event);
        return getQueue(queueType.getQueueName(topic)).offer(event);
    }

    private String getTopic(Event<?> event) {
        if (event.isRegisterOrgId()) {
            return SYSTEM_TOPIC;
        }
        return event.getOrgId();
    }

    public void register(QueueType queueType, String orgId, FintEventListener listener) {
        log.debug("Register {} {} {}", queueType, orgId, listener);
        final EventDispatcher dispatcher = dispatchers.computeIfAbsent(queueType.getQueueName(orgId), this::createDispatcher);
        dispatcher.registerListener(orgId, listener);
        if (!dispatcher.isRunning()) {
            executorService.execute(dispatcher);
        }
    }

    private EventDispatcher createDispatcher(String queueName) {
        return new EventDispatcher(getQueue(queueName), executorService);
    }

    private BlockingQueue<Event<?>> getQueue(String queueName) {
        return queues.computeIfAbsent(queueName, hazelcastInstance::getQueue);
    }

    public void clear() {
        dispatchers.values().forEach(EventDispatcher::clearListeners);
        dispatchers.clear();
        queues.values().forEach(BlockingQueue::clear);
        queues.clear();
    }
}

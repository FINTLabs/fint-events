package no.fint.events.internal;

import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.events.FintEventListener;
import org.springframework.util.StringUtils;

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
        final String queueName = queueType.getQueueName(getTopic(event));
        log.debug("Send to {}", queueName);
        log.trace("Send event {}", event);
        return getQueue(queueName).offer(event);
    }

    private String getTopic(Event<?> event) {
        if (!StringUtils.hasText(event.getOrgId())) {
            return SYSTEM_TOPIC;
        }
        return event.getOrgId();
    }

    public void register(QueueType queueType, String orgId, FintEventListener listener) {
        final String queueName = queueType.getQueueName(orgId);
        log.debug("Register {} {}", queueName, listener);
        final EventDispatcher dispatcher = dispatchers.computeIfAbsent(queueName, this::createDispatcher);
        dispatcher.registerListener(listener);
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

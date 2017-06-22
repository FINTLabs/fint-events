package no.fint.events;

import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.config.FintEventsProps;
import no.fint.events.event.RedissonReconnectedEvent;
import no.fint.events.queue.FintEventsQueue;
import no.fint.events.queue.QueueName;
import no.fint.events.scheduling.FintEventsScheduling;
import no.fint.events.scheduling.Listener;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@DependsOn("dockerRedis")
@Component
public class FintEvents implements ApplicationContextAware {
    public static final String REDISSON_TEMP_QUEUE_PREFIX = "temp-";

    private RedissonClient client;
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private FintEventsScheduling scheduling;

    @Autowired
    private FintEventsProps props;

    @Autowired
    private FintEventsQueue fintQueue;

    @Getter
    private List<Listener> listeners = new ArrayList<>();

    @Getter
    private Set<String> queues = new HashSet<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Config config = props.getRedissonConfig();
        client = Redisson.create(config);
    }

    @PreDestroy
    public void shutdown() {
        client.shutdown();
    }

    public void reconnect() {
        try {
            unregisterAllListeners();
            shutdown();
            init();

            for (Listener listener : listeners) {
                registerListener(listener.getQueueName(), listener.getObject().getClass());
            }
            publisher.publishEvent(new RedissonReconnectedEvent());
        } catch (RedisException e) {
            log.warn("Unable to reconnect to redis, {}", e.getMessage());
        }
    }

    public RedissonClient getClient() {
        return client;
    }

    public <V> RBlockingQueue<V> getTempQueue(String queue) {
        return client.getBlockingQueue(REDISSON_TEMP_QUEUE_PREFIX + queue);
    }

    public boolean deleteTempQueues() {
        RKeys keys = client.getKeys();
        Iterable<String> tempQueues = client.getKeys().getKeysByPattern(REDISSON_TEMP_QUEUE_PREFIX + "*");
        String[] tempQueuesArray = Iterables.toArray(tempQueues, String.class);
        if (tempQueuesArray.length == 0) {
            log.info("No temporary queues found");
            return true;
        }

        long keysDeleted = keys.delete(tempQueuesArray);
        if (keysDeleted == tempQueuesArray.length) {
            log.info("Deleted {} temp queues", keysDeleted);
            return true;
        } else {
            log.info("Deleted {} temp queues, the total number of queues are {}", keysDeleted, tempQueuesArray.length);
            return false;
        }
    }

    public <V> BlockingQueue<V> getQueue(String queue) {
        queues.add(queue);
        return client.getBlockingQueue(queue);
    }

    public <V> BlockingQueue<V> getDownstream(String orgId) {
        return getDownstream(QueueName.with(orgId));
    }

    public <V> BlockingQueue<V> getDownstream(QueueName queueName) {
        String downstreamQueueName = fintQueue.getDownstreamQueueName(queueName);
        return getQueue(downstreamQueueName);
    }

    public <V> BlockingQueue<V> getUpstream(String orgId) {
        return getUpstream(QueueName.with(orgId));
    }

    public <V> BlockingQueue<V> getUpstream(QueueName queueName) {
        String upstreamQueueName = fintQueue.getUpstreamQueueName(queueName);
        return getQueue(upstreamQueueName);
    }

    public void send(String queue, Object value) {
        getQueue(queue).offer(value);
    }

    public void sendDownstream(String orgId, Object value) {
        sendDownstream(QueueName.with(orgId), value);
    }

    public void sendDownstream(QueueName queueName, Object value) {
        getDownstream(queueName).offer(value);
    }

    public void sendUpstream(String orgId, Object value) {
        sendUpstream(QueueName.with(orgId), value);
    }

    public void sendUpstream(QueueName queueName, Object value) {
        getUpstream(queueName).offer(value);
    }

    public Optional<String> registerDownstreamListener(Class<?> listener, String orgId) {
        String downstreamQueueName = fintQueue.getDownstreamQueueName(QueueName.with(orgId));
        return registerListener(downstreamQueueName, listener);
    }

    public List<String> registerDownstreamListener(Class<?> listener, String... orgIds) {
        return registerDownstreamListener(listener, toQueueNameArray(orgIds));
    }

    public List<String> registerDownstreamListener(Class<?> listener, QueueName... queueNames) {
        List<String> listenerIds = new ArrayList<>();
        for (QueueName queueName : queueNames) {
            String downstreamQueueName = fintQueue.getDownstreamQueueName(queueName);
            Optional<String> listenerId = registerListener(downstreamQueueName, listener);
            listenerId.ifPresent(listenerIds::add);
        }
        return listenerIds;
    }

    public Optional<String> registerUpstreamListener(Class<?> listener, String orgId) {
        String upstreamQueueName = fintQueue.getUpstreamQueueName(QueueName.with(orgId));
        return registerListener(upstreamQueueName, listener);
    }

    public List<String> registerUpstreamListener(Class<?> listener, String... orgIds) {
        return registerUpstreamListener(listener, toQueueNameArray(orgIds));
    }

    public List<String> registerUpstreamListener(Class<?> listener, QueueName... queueNames) {
        List<String> listenerIds = new ArrayList<>();
        for (QueueName queueName : queueNames) {
            String upstreamQueueName = fintQueue.getUpstreamQueueName(queueName);
            Optional<String> listenerId = registerListener(upstreamQueueName, listener);
            listenerId.ifPresent(listenerIds::add);
        }
        return listenerIds;
    }

    private QueueName[] toQueueNameArray(String... orgIds) {
        QueueName[] queueNames = new QueueName[orgIds.length];
        for (int i = 0; i < orgIds.length; i++) {
            queueNames[i] = QueueName.with(orgIds[i]);
        }
        return queueNames;
    }

    public Optional<String> registerListener(String queue, Class<?> listener) {
        Optional<String> listenerId = Optional.empty();
        Object bean = applicationContext.getBean(listener);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            FintEventListener annotation = method.getAnnotation(FintEventListener.class);

            if (annotation != null) {
                Listener listenerInstance = new Listener(bean, method, queue, getQueue(queue));

                if (!listeners.contains(listenerInstance)) {
                    log.info("Registering listener ({}) for {}", listener.getSimpleName(), queue);
                    listenerId = Optional.of(listenerInstance.getId());
                    scheduling.register(listenerInstance);
                    listeners.add(listenerInstance);
                }
            }
        }
        return listenerId;
    }

    public boolean unregisterListener(String listenerId) {
        Optional<Listener> listener = listeners.stream().filter(l -> l.getId().equals(listenerId)).findAny();
        if (listener.isPresent()) {
            scheduling.unregister(listenerId);
            listeners.remove(listener.get());
            return true;
        }
        return false;
    }

    public void unregisterAllListeners() {
        listeners.clear();
        scheduling.unregisterAllListeners();
    }

    public List<String> getListenerIds() {
        return listeners.stream().map(Listener::getId).collect(Collectors.toList());
    }
}

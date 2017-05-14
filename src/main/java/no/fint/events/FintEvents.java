package no.fint.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.config.FintEventsProps;
import no.fint.events.config.FintEventsScheduling;
import no.fint.events.listener.Listener;
import no.fint.events.queue.FintEventsQueue;
import no.fint.events.queue.QueueName;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@Slf4j
@DependsOn("embeddedRedis")
@Component
public class FintEvents implements ApplicationContextAware {
    public static final String REDISSON_QUEUES_KEY = "fintQueues";

    private RedissonClient client;
    private ApplicationContext applicationContext;

    @Autowired
    private FintEventsScheduling scheduling;

    @Autowired
    private FintEventsProps props;

    @Autowired
    private FintEventsQueue fintQueue;

    @Getter
    private Map<String, Long> listeners = new HashMap<>();

    @Getter
    private Set<String> componentQueues = new HashSet<>();

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
        queues = client.getSet(REDISSON_QUEUES_KEY);
    }

    @PreDestroy
    public void shutdown() {
        client.shutdown();
    }

    public RedissonClient getClient() {
        return client;
    }

    public <V> BlockingQueue<V> getTempQueue(String queue) {
        return client.getBlockingQueue(queue);
    }

    public <V> BlockingQueue<V> getQueue(String queue) {
        componentQueues.add(queue);
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

    public void registerDownstreamListener(Class<?> listener, String... orgIds) {
        registerDownstreamListener(listener, toQueueNameArray(orgIds));
    }

    public void registerDownstreamListener(Class<?> listener, QueueName... queueNames) {
        for (QueueName queueName : queueNames) {
            log.info("Registering downstream listener ({}) for {}", listener.getSimpleName(), queueName.getOrgId());
            String downstreamQueueName = fintQueue.getDownstreamQueueName(queueName);
            registerListener(downstreamQueueName, listener);
        }
    }

    public void registerUpstreamListener(Class<?> listener, String... orgIds) {
        registerUpstreamListener(listener, toQueueNameArray(orgIds));
    }

    public void registerUpstreamListener(Class<?> listener, QueueName... queueNames) {
        for (QueueName queueName : queueNames) {
            log.info("Registering upstream listener ({}) for {}", listener.getSimpleName(), queueName.getOrgId());
            String upstreamQueueName = fintQueue.getUpstreamQueueName(queueName);
            registerListener(upstreamQueueName, listener);
        }
    }

    private QueueName[] toQueueNameArray(String... orgIds) {
        QueueName[] queueNames = new QueueName[orgIds.length];
        for (int i = 0; i < orgIds.length; i++) {
            queueNames[i] = QueueName.with(orgIds[i]);
        }
        return queueNames;
    }

    public void registerListener(String queue, Class<?> listener) {
        Object bean = applicationContext.getBean(listener);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            FintEventListener annotation = method.getAnnotation(FintEventListener.class);
            if (annotation != null) {
                Listener listenerInstance = new Listener(bean, method, getQueue(queue));
                scheduling.register(listenerInstance);
                listeners.put(queue, System.currentTimeMillis());
            }
        }
    }
}

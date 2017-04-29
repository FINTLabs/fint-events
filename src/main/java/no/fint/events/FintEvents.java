package no.fint.events;

import no.fint.events.annotations.FintEventsListener;
import no.fint.events.config.FintEventsProps;
import no.fint.events.config.RedisConfiguration;
import no.fint.events.listener.Listener;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.TaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

public class FintEvents implements ApplicationContextAware {
    private RedissonClient client;
    private ApplicationContext applicationContext;

    @Autowired
    private FintEventsProps props;

    @Autowired
    private TaskScheduler taskScheduler;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        client = createRedissonClient();
    }

    @PreDestroy
    public void shutdown() {
        client.shutdown();
    }

    RedissonClient createRedissonClient() {
        Config config = new Config();
        String redisConfiguration = props.getRedisConfiguration();
        if (RedisConfiguration.isSingle(redisConfiguration)) {
            config.useSingleServer().setAddress(props.getRedisAddress());
        } else {
            throw new IllegalArgumentException(String.format("The redis-configuration %s is not supported", redisConfiguration));
        }

        return Redisson.create(config);
    }

    public RedissonClient getClient() {
        return client;
    }

    public <V> BlockingQueue<V> getQueue(String queue) {
        return client.getBlockingQueue(queue);
    }

    public <V> BlockingQueue<V> getDownstream(String orgId) {
        String downstream = props.getDefaultDownstreamQueue();
        return getQueue(String.format(downstream, orgId));
    }

    public <V> BlockingQueue<V> getUpstream(String orgId) {
        String upstream = props.getDefaultUpstreamQueue();
        return getQueue(String.format(upstream, orgId));
    }

    public void send(String queue, Object value) {
        getQueue(queue).offer(value);
    }

    public void sendDownstream(String orgId, Object value) {
        getDownstream(orgId).offer(value);
    }

    public void sendUpstream(String orgId, Object value) {
        getUpstream(orgId).offer(value);
    }

    public void registerDownstreamListener(String orgId, Class<?> listener) {
        String downstream = props.getDefaultDownstreamQueue();
        registerListener(String.format(downstream, orgId), listener);
    }

    public void registerUpstreamListener(String orgId, Class<?> listener) {
        String upstream = props.getDefaultUpstreamQueue();
        registerListener(String.format(upstream, orgId), listener);
    }

    public void registerListener(String queue, Class<?> listener) {
        Object bean = applicationContext.getBean(listener);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            FintEventsListener annotation = method.getAnnotation(FintEventsListener.class);
            if (annotation != null) {
                Listener listenerInstance = new Listener(bean, method, getQueue(queue));
                taskScheduler.scheduleWithFixedDelay(listenerInstance, 10);
            }
        }
    }
}

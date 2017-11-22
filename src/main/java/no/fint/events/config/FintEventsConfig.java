package no.fint.events.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import no.fint.event.model.Event;
import no.fint.events.FintEvents;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
import no.fint.events.internal.QueueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;

@Configuration
@ComponentScan(basePackageClasses = FintEvents.class)
public class FintEventsConfig {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @Qualifier("no.fint.events.downstream")
    @Bean
    public EventDispatcher downstreamEventDispatcher() {
        return new EventDispatcher(downstreamQueue());
    }

    @Qualifier("no.fint.events.upstream")
    @Bean
    public EventDispatcher upstreamEventDispatcher() {
        return new EventDispatcher(upstreamQueue());
    }

    @Bean
    public BlockingQueue<Event> downstreamQueue() {
        return hazelcastInstance.getQueue(QueueType.DOWNSTREAM.getQueueName());
    }

    @Bean
    public BlockingQueue<Event> upstreamQueue() {
        IQueue<Event> queue = hazelcastInstance.getQueue(QueueType.UPSTREAM.getQueueName());
        queue.addItemListener(fintEventsHealth, true);
        return queue;
    }
}

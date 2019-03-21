package no.fint.events.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import no.fint.event.model.Event;
import no.fint.events.FintEvents;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
import no.fint.events.internal.FintEventsSync;
import no.fint.events.internal.QueueType;
import no.fint.hazelcast.FintHazelcastConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FintHazelcastConfig.class)
@ComponentScan(basePackageClasses = FintEvents.class)
public class FintEventsConfig {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @Autowired
    private FintEventsSync fintEventsSync;

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents(downstreamEventDispatcher(), upstreamEventDispatcher(), fintEventsHealth, fintEventsSync);
    }

    @Bean
    public EventDispatcher downstreamEventDispatcher() {
        IQueue<Event> queue = hazelcastInstance.getQueue(QueueType.DOWNSTREAM.getQueueName());
        return new EventDispatcher(queue);
    }

    @Bean
    public EventDispatcher upstreamEventDispatcher() {
        IQueue<Event> queue = hazelcastInstance.getQueue(QueueType.UPSTREAM.getQueueName());
        queue.addItemListener(fintEventsHealth, true);
        queue.addItemListener(fintEventsSync, true);
        return new EventDispatcher(queue);
    }

}

package no.fint.events.config;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import no.fint.event.model.Event;
import no.fint.events.FintEvents;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
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

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents(downstreamEventDispatcher(), upstreamEventDispatcher(), fintEventsHealth);
    }

    @Bean
    public EventDispatcher downstreamEventDispatcher() {
        ITopic<Event> topic = hazelcastInstance.getTopic(QueueType.DOWNSTREAM.getQueueName());
        return new EventDispatcher(topic);
    }

    @Bean
    public EventDispatcher upstreamEventDispatcher() {
        ITopic<Event> topic = hazelcastInstance.getTopic(QueueType.UPSTREAM.getQueueName());
        topic.addMessageListener(fintEventsHealth);
        return new EventDispatcher(topic);
    }

}

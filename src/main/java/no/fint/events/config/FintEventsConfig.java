package no.fint.events.config;

import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import no.fint.event.model.Event;
import no.fint.events.FintEvents;
import no.fint.events.internal.EventDispatcher;
import no.fint.events.internal.FintEventsHealth;
import no.fint.events.internal.QueueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ComponentScan(basePackageClasses = FintEvents.class)
public class FintEventsConfig {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @Autowired
    private FintEventsProps props;

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents(downstreamEventDispatcher(), upstreamEventDispatcher(), fintEventsHealth);
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
        return new EventDispatcher(queue);
    }

    @Bean
    @ConditionalOnProperty(name = FintEventsProps.FINT_HAZELCAST_MEMBERS)
    public Config hazelcastConfig() {
        Config cfg = new ClasspathXmlConfig(props.getHazelcastConfig());
        return cfg.setNetworkConfig(createNetworkConfig());
    }

    private NetworkConfig createNetworkConfig() {
        TcpIpConfig tcpIpConfig = new TcpIpConfig().setMembers(Arrays.asList(props.getHazelcastMembers()));
        tcpIpConfig.setEnabled(true);
        return new NetworkConfig()
                .setJoin(new JoinConfig().setTcpIpConfig(tcpIpConfig)
                        .setMulticastConfig(new MulticastConfig().setEnabled(false)));
    }

}

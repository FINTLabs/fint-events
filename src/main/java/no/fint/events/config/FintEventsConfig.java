package no.fint.events.config;

import com.hazelcast.core.HazelcastInstance;
import no.fint.events.FintEvents;
import no.fint.events.internal.QueueService;
import no.fint.hazelcast.FintHazelcastConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FintHazelcastConfig.class)
@ComponentScan(basePackageClasses = FintEvents.class)
public class FintEventsConfig {

    @Bean
    public FintEvents fintEvents(QueueService queueService) {
        return new FintEvents(queueService);
    }

    @Bean
    public QueueService queueService(HazelcastInstance hazelcastInstance) {
        return new QueueService(hazelcastInstance);
    }

}

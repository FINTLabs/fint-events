package no.fint.events.queue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicQueuesConfig {

    @Bean
    public DynamicQueuesRegistry dynamicQueuesRegistry() {
        return new DynamicQueuesRegistry();
    }

    @Bean
    public DynamicQueues dynamicQueues() {
        return new DynamicQueues();
    }
}

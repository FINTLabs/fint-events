package no.fint.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EventsConfig {

    @Bean
    public EventsRegistry eventsRegistry() {
        return new EventsRegistry();
    }

    @Bean
    public Events eventsQueues() {
        return new Events();
    }

    @Profile("!test")
    @Bean
    public FintEvents fintEvents() {
        return new FintEvents();
    }
}

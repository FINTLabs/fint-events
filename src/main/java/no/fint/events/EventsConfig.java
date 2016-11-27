package no.fint.events;

import no.fint.events.local.LocalRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(LocalRabbit.class)
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

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents();
    }

    @Bean
    public EventsProps eventsProps() {
        return new EventsProps();
    }
}

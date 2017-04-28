package no.fint.events.config;

import no.fint.events.FintEvents;
import no.fint.events.FintEventsRemote;
import no.fint.events.testmode.EmbeddedRedis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class FintEventsConfig {

    @Bean
    public FintEventsProps fintEventsProps() {
        return new FintEventsProps();
    }

    @Bean
    public EmbeddedRedis embeddedRedis() {
        return new EmbeddedRedis();
    }

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents();
    }

    @Bean
    public FintEventsRemote fintEventsRemoteService() {
        return new FintEventsRemote();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

}

package no.fint.events.config;

import no.fint.events.FintEvents;
import no.fint.events.FintEventsHealth;
import no.fint.events.controller.FintEventsController;
import no.fint.events.remote.FintEventsRemote;
import no.fint.events.testmode.EmbeddedRedis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan(basePackageClasses = FintEventsController.class)
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
    @DependsOn("embeddedRedis")
    public FintEvents fintEvents() {
        return new FintEvents();
    }

    @Bean
    public FintEventsHealth fintEventsHealth() {
        return new FintEventsHealth();
    }

    @Bean
    public FintEventsRemote fintEventsRemote() {
        return new FintEventsRemote();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

}

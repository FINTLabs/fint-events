package no.fint.events.config;

import no.fint.events.FintEvents;
import no.fint.events.FintEventsHealth;
import no.fint.events.controller.FintEventsController;
import no.fint.events.remote.FintEventsRemote;
import no.fint.events.testmode.EmbeddedRedis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@Configuration
public class FintEventsConfig implements SchedulingConfigurer {

    @Value("${fint.events.task-scheduler-thread-pool-size:50}")
    private int taskSchedulerThreadPoolSize;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(taskSchedulerThreadPoolSize);
        taskScheduler.initialize();
        registrar.setTaskScheduler(taskScheduler);
        fintEventsScheduling().setRegistrar(registrar);
    }

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
    public FintEventsScheduling fintEventsScheduling() {
        return new FintEventsScheduling();
    }

    @Bean
    @ConditionalOnProperty(value = FintEventsProps.QUEUE_ENDPOINT_ENABLED, havingValue = "true")
    public FintEventsController fintEventsController() {
        return new FintEventsController();
    }

}

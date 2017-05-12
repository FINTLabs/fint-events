package no.fint.events.config;

import no.fint.events.FintEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@Configuration
@ComponentScan(basePackageClasses = FintEvents.class)
public class FintEventsConfig implements SchedulingConfigurer {

    @Value("${fint.events.task-scheduler-thread-pool-size:50}")
    private int taskSchedulerThreadPoolSize;

    @Autowired
    private FintEventsScheduling fintEventsScheduling;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(taskSchedulerThreadPoolSize);
        taskScheduler.initialize();
        registrar.setTaskScheduler(taskScheduler);
        fintEventsScheduling.setRegistrar(registrar);
    }

}

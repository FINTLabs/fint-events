package no.fint.events.scheduling;

import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FintEventsScheduling {
    private ScheduledTaskRegistrar registrar;

    private List<Listener> tempListeners = new ArrayList<>();
    private Map<String, ScheduledTask> listenerTasks = new HashMap<>();

    public void setRegistrar(ScheduledTaskRegistrar registrar) {
        this.registrar = registrar;
        tempListeners.forEach(this::register);
        tempListeners.clear();
    }

    public void register(Listener listener) {
        if (registrar == null) {
            tempListeners.add(listener);
        } else {
            IntervalTask intervalTask = new IntervalTask(listener, 10);
            ScheduledTask scheduledTask = registrar.scheduleFixedDelayTask(intervalTask);
            listenerTasks.put(listener.getId(), scheduledTask);
        }
    }

    public void unregister(String listenerId) {
        ScheduledTask scheduledTask = listenerTasks.get(listenerId);
        if (scheduledTask != null) {
            scheduledTask.cancel();
            listenerTasks.remove(listenerId);
        }
    }

    public void unregisterAllListeners() {
        listenerTasks.values().forEach(ScheduledTask::cancel);
        listenerTasks.clear();
    }

}

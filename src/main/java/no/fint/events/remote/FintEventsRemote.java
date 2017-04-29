package no.fint.events.remote;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import org.redisson.api.RRemoteService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class FintEventsRemote implements ApplicationContextAware {
    @Autowired
    private FintEvents fintEvents;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void registerServer(Class<? extends RemoteEvent> type) {
        RemoteEvent bean = applicationContext.getBean(type);
        RRemoteService remoteService = fintEvents.getClient().getRemoteService();
        remoteService.register(RemoteEvent.class, bean);
    }

    @SuppressWarnings("unchecked")
    public <V> RemoteEvent<V> registerClient() {
        RRemoteService remoteService = fintEvents.getClient().getRemoteService();
        return remoteService.get(RemoteEvent.class);
    }

}

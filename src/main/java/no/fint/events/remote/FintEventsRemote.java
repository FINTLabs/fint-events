package no.fint.events.remote;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import org.redisson.api.RRemoteService;
import org.redisson.api.RemoteInvocationOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FintEventsRemote implements ApplicationContextAware {
    @Autowired
    private FintEvents fintEvents;
    private ApplicationContext applicationContext;
    private RemoteInvocationOptions options;

    @PostConstruct
    public void init() {
        options = RemoteInvocationOptions.defaults().expectAckWithin(10, TimeUnit.SECONDS).expectResultWithin(5, TimeUnit.MINUTES);
    }

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
        return remoteService.get(RemoteEvent.class, options);
    }

}

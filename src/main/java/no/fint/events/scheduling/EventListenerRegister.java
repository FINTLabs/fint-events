package no.fint.events.scheduling;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.config.FintEventsProps;
import no.fint.events.queue.QueueType;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Order
@Component
public class EventListenerRegister implements ApplicationContextAware {
    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private FintEventsProps props;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        if (props.getOrgIds().length > 0) {
            Optional<EventListenerMetadata> metadata = getListenerMetadata();
            metadata.ifPresent(this::registerListener);
        }
    }

    Optional<EventListenerMetadata> getListenerMetadata() {
        try {
            List<EventListenerMetadata> listeners = getListeners();
            if (listeners.size() == 1) {
                return Optional.of(listeners.get(0));
            } else if (listeners.size() == 0) {
                log.info("No event listeners found, will not register default orgIds");
            } else {
                log.info("Multiple listeners found, will not register a default listener for orgIds");
            }
        } catch (RuntimeException e) {
            log.warn("Exception when trying to find default listener, {}", e.getMessage());
        }

        return Optional.empty();
    }

    private List<EventListenerMetadata> getListeners() {
        List<EventListenerMetadata> listeners = new ArrayList<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            EventListenerMetadata listener = getListener(beanName);
            if (listener != null) {
                listeners.add(listener);
            }
        }
        return listeners;
    }

    private EventListenerMetadata getListener(String beanName) {
        Object bean = applicationContext.getBean(beanName);
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Method[] methods = targetClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(FintEventListener.class)) {
                return new EventListenerMetadata(targetClass, method);
            }
        }
        return null;
    }

    public void registerListener(EventListenerMetadata metadata) {
        FintEventListener annotation = metadata.getMethod().getAnnotation(FintEventListener.class);
        String type = annotation.type();
        if (StringUtils.isEmpty(type)) {
            log.warn("No type registered for event listener {}, skipping default registration of event listener", metadata.getClazz().getName());
        } else {
            switch (type) {
                case QueueType.DOWNSTREAM:
                    log.info("Registering downstream listener for orgId(s) {}", (Object) props.getOrgIds());
                    fintEvents.registerDownstreamListener(metadata.getClazz(), props.getOrgIds());
                    break;
                case QueueType.UPSTREAM:
                    log.info("Registering upstream listener for orgId(s) {}", (Object) props.getOrgIds());
                    fintEvents.registerUpstreamListener(metadata.getClazz(), props.getOrgIds());
                    break;
                default:
                    log.warn("Queue type {} is not found, skipping default registration of event listener", type);
                    break;
            }
        }
    }
}

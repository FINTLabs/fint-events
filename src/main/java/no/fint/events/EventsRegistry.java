package no.fint.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.listeners.EventsHeaderAndBodyListener;
import no.fint.events.listeners.EventsJsonObjectListener;
import no.fint.events.listeners.EventsMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class EventsRegistry implements ApplicationContextAware {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationContext applicationContext;

    private Map<String, SimpleMessageListenerContainer> registeredContainers = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    Optional<SimpleMessageListenerContainer> add(String queue, Class<?> listener) {
        if (registeredContainers.get(queue) != null) {
            return Optional.empty();
        }

        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.addQueueNames(queue);
        Object bean = applicationContext.getBean(listener);

        Optional<Method> messageListener = getMessageListenerMethod(listener);
        Optional<Method> headerAndBodyListener = getHeaderAndBodyListenerMethod(listener);
        Optional<Method> jsonObjectListener = getJsonObjectMethod(listener);
        if (messageListener.isPresent()) {
            listenerContainer.setMessageListener(new EventsMessageListener(bean, messageListener.get().getName()));
            log.info("Registering message listener method: {}", messageListener.get().getName());
        } else if (headerAndBodyListener.isPresent()) {
            listenerContainer.setMessageListener(new EventsHeaderAndBodyListener(bean, headerAndBodyListener.get().getName()));
            log.info("Registering header and body listener method: {}", headerAndBodyListener.get().getName());
        } else if (jsonObjectListener.isPresent()) {
            listenerContainer.setMessageListener(new EventsJsonObjectListener(jsonObjectListener.get().getParameterTypes()[0], bean, jsonObjectListener.get().getName()));
            log.info("Registering json object listener method: {}", jsonObjectListener.get().getName());
        } else {
            Optional<Method> publicMethod = getPublicMethod(listener);
            if (publicMethod.isPresent()) {
                log.info("Registering listener method: {}", publicMethod.get().getName());
                listenerContainer.setMessageListener(new MessageListenerAdapter(bean, publicMethod.get().getName()));
            } else {
                throw new IllegalStateException("Unable to find any listener methods, " + listener);
            }
        }

        registeredContainers.put(queue, listenerContainer);
        log.info("Registered {} to listen on queue {}", listener.getSimpleName(), queue);
        return Optional.of(listenerContainer);
    }

    Optional<Method> getMessageListenerMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> (Modifier.isPublic(method.getModifiers())))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == Message.class)
                .findAny();
    }

    Optional<Method> getHeaderAndBodyListenerMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> (Modifier.isPublic(method.getModifiers())))
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == Map.class)
                .filter(method -> method.getParameterTypes()[1] == byte[].class)
                .findAny();
    }

    Optional<Method> getJsonObjectMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> (Modifier.isPublic(method.getModifiers())))
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] != byte[].class)
                .filter(method -> !"equals".equals(method.getName()))
                .findAny();
    }

    Optional<Method> getPublicMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> (Modifier.isPublic(method.getModifiers())))
                .filter(method -> method.getParameterCount() == 1)
                .findFirst();
    }

    void close(String queue) {
        if (registeredContainers.containsKey(queue)) {
            SimpleMessageListenerContainer listenerContainer = registeredContainers.get(queue);
            listenerContainer.stop();
            registeredContainers.remove(queue);
        } else {
            log.warn("The key {} was not found in the registered listeners", queue);
        }
    }

    void shutdown() {
        registeredContainers.values().stream().findFirst().ifPresent(AbstractMessageListenerContainer::shutdown);
    }

    boolean containsListener(String queue) {
        return registeredContainers.containsKey(queue);
    }
}

package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class EventsRegistry implements ApplicationContextAware {

    @Autowired
    private ConnectionFactory connectionFactory;

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
    }

    Optional<SimpleMessageListenerContainer> add(String queue, Class<?> listener) {
        if (!containsListener(queue)) {
            Optional<Method> method = getMessageListenerMethod(listener);
            SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);

            listenerContainer.addQueueNames(queue);
            Object bean = beanFactory.getBean(listener);
            
            if (method.isPresent()) {
                listenerContainer.setMessageListener(new EventsMessageListener(bean, method.get().getName()));
            } else {
                log.info("No method in the listener found with Message as input parameter, using the standard MessageListenerAdapter");
                Optional<Method> publicMethod = getPublicMethod(listener);
                if (publicMethod.isPresent()) {
                    listenerContainer.setMessageListener(new MessageListenerAdapter(bean, publicMethod.get().getName()));
                } else {
                    throw new IllegalStateException("Unable to find any listener methods, " + listener);
                }
            }

            beanFactory.registerSingleton(queue, listenerContainer);
            log.info("Registered {} to listen on queue {}", listener.getSimpleName(), queue);

            return Optional.of(listenerContainer);
        }

        return Optional.empty();
    }

    Optional<Method> getMessageListenerMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods)
                .filter(method -> (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 1))
                .filter(method -> method.getParameterTypes()[0] == Message.class)
                .findAny();
    }

    Optional<Method> getPublicMethod(Class<?> listener) {
        Method[] methods = listener.getDeclaredMethods();
        return Arrays.stream(methods).filter(method -> (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 1)).findFirst();
    }

    void shutdown(String queue) {
        if (containsListener(queue)) {
            SimpleMessageListenerContainer bean = beanFactory.getBean(queue, SimpleMessageListenerContainer.class);
            bean.shutdown();
        }
    }

    void shutdown() {
        Map<String, SimpleMessageListenerContainer> beans = beanFactory.getBeansOfType(SimpleMessageListenerContainer.class);
        beans.values().forEach(SimpleMessageListenerContainer::shutdown);
    }

    boolean containsListener(String queue) {
        return beanFactory.containsBean(queue);
    }
}

package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class EventsRegistry implements ApplicationContextAware {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private EventsProps eventsProps;

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
    }

    void add(String queue, Class<?> listener) {
        if (!containsListener(queue)) {
            Optional<Method> method = getMessageListenerMethod(listener);
            SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);

            listenerContainer.addQueueNames(queue);
            try {
                if (method.isPresent()) {
                    listenerContainer.setMessageListener(new EventsMessageListener(listener.newInstance(), method.get().getName()));
                } else {
                    log.info("No method in the listener found with Message as input parameter, using the standard MessageListenerAdapter");
                    Optional<Method> publicMethod = getPublicMethod(listener);
                    if (publicMethod.isPresent()) {
                        listenerContainer.setMessageListener(new MessageListenerAdapter(listener.newInstance(), publicMethod.get().getName()));
                    } else {
                        throw new IllegalStateException("Unable to find any listener methods, " + listener);
                    }
                }

                addContainerProperties(listenerContainer);
                beanFactory.registerSingleton(queue, listenerContainer);
                log.info("Registered {} to listen on queue {}", listener.getSimpleName(), queue);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("Unable to create new instance of " + listener.getName(), e);
            }
        }
    }

    private void addContainerProperties(SimpleMessageListenerContainer listenerContainer) {
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(eventsProps.getRetryMaxAttempts())
                .backOffOptions(eventsProps.getRetryInitialInterval(), eventsProps.getRetryMultiplier(), eventsProps.getRetryMaxInterval())
                .recoverer(new RepublishMessageRecoverer(new RabbitTemplate(connectionFactory), "deadletter.exchange", "deadletter.queue"))
                .build();
        listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
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

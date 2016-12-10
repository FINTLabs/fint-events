package no.fint.events.fintevents;

import no.fint.events.EventType;
import no.fint.events.Events;
import no.fint.events.properties.ListenerProps;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.Optional;

public class FintListeners {

    @Autowired
    private Events events;

    @Autowired
    private ListenerProps listenerProps;

    public void register(Class<?> listener, EventType eventType, FintOrganisation org) {
        Queue queue = org.getQueue(eventType);
        Optional<SimpleMessageListenerContainer> listenerContainer = events.registerUnstartedListener(org.getExchange(), queue, listener);
        if (listenerContainer.isPresent()) {
            addRetry(org, listenerContainer.get());
            addAcknowledgeMode(listenerContainer.get());
            listenerContainer.get().start();
        }
    }

    private void addRetry(FintOrganisation org, SimpleMessageListenerContainer listenerContainer) {
        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(listenerProps.getRetryMaxAttempts())
                .backOffOptions(listenerProps.getRetryInitialInterval(), listenerProps.getRetryMultiplier(), listenerProps.getRetryMaxInterval())
                .recoverer(new RepublishMessageRecoverer(events.rabbitTemplate(), org.getExchangeName(), org.getErrorQueueName()))
                .build();
        listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
    }

    private void addAcknowledgeMode(SimpleMessageListenerContainer listenerContainer) {
        String acknowledgeModeProp = listenerProps.getAcknowledgeMode();
        AcknowledgeMode acknowledgeMode = AcknowledgeMode.valueOf(acknowledgeModeProp);
        listenerContainer.setAcknowledgeMode(acknowledgeMode);
    }
}

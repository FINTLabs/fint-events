package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.properties.RabbitProps;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class Events {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private EventsRegistry eventsRegistry;

    @Autowired
    private RabbitProps rabbitProps;

    @PostConstruct
    public void init() {
        connectionFactory.addConnectionListener(new ConnectionListener() {
            @Override
            public void onCreate(Connection connection) {
                log.info("Connecting on local port: {}", connection.getLocalPort());
            }

            @Override
            public void onClose(Connection connection) {
                log.info("Close connection on local port: {}", connection.getLocalPort());
            }
        });
    }

    public Optional<SimpleMessageListenerContainer> registerListener(String exchange, String queue, Class<?> listener) {
        return registerListener(new TopicExchange(exchange), new Queue(queue), listener);
    }

    public Optional<SimpleMessageListenerContainer> registerListener(TopicExchange exchange, Queue queue, Class<?> listener) {
        addQueues(exchange, queue);
        Optional<SimpleMessageListenerContainer> listenerContainer = eventsRegistry.add(queue.getName(), listener);
        listenerContainer.ifPresent(AbstractMessageListenerContainer::start);
        return listenerContainer;
    }

    public Optional<SimpleMessageListenerContainer> registerUnstartedListener(TopicExchange exchange, Queue queue, Class<?> listener) {
        addQueues(exchange, queue);
        return eventsRegistry.add(queue.getName(), listener);
    }

    public void addQueues(TopicExchange exchange, Queue... queues) {
        amqpAdmin.declareExchange(exchange);
        Arrays.stream(queues).forEach(queue -> {
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(getBinding(exchange, queue));
        });
    }

    public void deleteQueues(TopicExchange exchange, Queue... queues) {
        Arrays.stream(queues).forEach(queue -> {
            boolean deleted = amqpAdmin.deleteQueue(queue.getName());
            if (deleted) {
                log.info("Deleted queue {}", queue.getName());
            }
        });
        boolean deleted = amqpAdmin.deleteExchange(exchange.getName());
        if (deleted) {
            log.info("Deleted exchange {}", exchange.getName());
        }
    }

    public void removeListener(String exchange, String queue) {
        removeListener(new TopicExchange(exchange), new Queue(queue));
    }

    public void removeListener(TopicExchange exchange, Queue queue) {
        amqpAdmin.removeBinding(getBinding(exchange, queue));
        eventsRegistry.close(queue.getName());
    }

    public boolean containsListener(String queue) {
        return eventsRegistry.containsListener(queue);
    }

    public void send(String queue, Object message, Class<?> type) {
        RabbitTemplate rabbitTemplate = rabbitTemplate(type);
        rabbitTemplate.convertAndSend(queue, message);
    }

    @SuppressWarnings("unchecked")
    public <T> T sendAndReceive(String exchange, String queue, Object message, Class<T> type) {
        RabbitTemplate rabbitTemplate = rabbitTemplate(type);
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(queue);
        rabbitTemplate.setReplyTimeout(rabbitProps.getReplyToTimeout());

        return (T) rabbitTemplate.convertSendAndReceive(message);
    }

    private Binding getBinding(TopicExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(queue.getName());
    }

    public RabbitTemplate rabbitTemplate(Class<?> type) {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        defaultClassMapper.setDefaultType(type);
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }

    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory);
    }
}

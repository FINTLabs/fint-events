package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;

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

    public Optional<SimpleMessageListenerContainer> registerListener(String exchange, String queue, Class<?> listener) {
        return registerListener(new TopicExchange(exchange), new Queue(queue), listener);
    }

    public Optional<SimpleMessageListenerContainer> registerListener(TopicExchange exchange, Queue queue, Class<?> listener) {
        addQueues(exchange, queue);
        Optional<SimpleMessageListenerContainer> listenerContainer = eventsRegistry.add(queue.getName(), listener);
        listenerContainer.ifPresent(AbstractMessageListenerContainer::start);
        return listenerContainer;
    }

    Optional<SimpleMessageListenerContainer> registerUnstartedListener(TopicExchange exchange, Queue queue, Class<?> listener) {
        addQueues(exchange, queue);
        return eventsRegistry.add(queue.getName(), listener);
    }

    void addQueues(TopicExchange exchange, Queue... queues) {
        amqpAdmin.declareExchange(exchange);
        Arrays.stream(queues).forEach(queue -> {
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(getBinding(exchange, queue));
        });
    }

    void deleteQueues(TopicExchange exchange, Queue... queues) {
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

    public void send(String queue, String message) {
        RabbitTemplate rabbitTemplate = rabbitTemplate(queue);
        rabbitTemplate.convertAndSend(queue, message);
    }

    public Message sendAndReceive(String exchange, String queue, String id, String message) {
        RabbitTemplate rabbitTemplate = rabbitTemplate();
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setReplyTimeout(-1);
        return rabbitTemplate.sendAndReceive(queue, new Message(message.getBytes(), new MessageProperties()), new CorrelationData(id));
    }

    private Binding getBinding(TopicExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(queue.getName());
    }

    public RabbitTemplate rabbitTemplate(String queue) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setQueue(queue);
        rabbitTemplate.setRoutingKey(queue);
        return rabbitTemplate;
    }

    public RabbitTemplate rabbitTemplate(Queue queue) {
        return rabbitTemplate(queue.getName());
    }

    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory);
    }
}

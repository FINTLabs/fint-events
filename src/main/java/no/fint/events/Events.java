package no.fint.events;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Optional;

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
            amqpAdmin.deleteQueue(queue.getName());
        });
        amqpAdmin.declareExchange(exchange);
    }

    public void removeListener(String exchange, String queue) {
        removeListener(new TopicExchange(exchange), new Queue(queue));
    }

    public void removeListener(TopicExchange exchange, Queue queue) {
        amqpAdmin.removeBinding(getBinding(exchange, queue));
        eventsRegistry.shutdown(queue.getName());
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

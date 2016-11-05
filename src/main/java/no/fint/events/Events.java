package no.fint.events;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class Events {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private EventsRegistry eventsRegistry;

    public void registerListener(String exchange, String queue, Class<?> listener) {
        registerListener(new TopicExchange(exchange), new Queue(queue), listener);
    }

    public void registerListener(TopicExchange exchange, Queue queue, Class<?> listener) {
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(getBinding(exchange, queue));
        eventsRegistry.add(queue.getName(), listener);
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
}

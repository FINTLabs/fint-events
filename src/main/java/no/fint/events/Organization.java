package no.fint.events;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

class Organization {
    private final String name;

    private String exchange;
    private String inputQueue;
    private String outputQueue;
    private String errorQueue;

    public Organization(String name, String defaultInputQueue, String defaultOutputQueue, String defaultErrorQueue) {
        this.name = name;
        this.exchange = name;
        this.inputQueue = String.format(defaultInputQueue, name);
        this.outputQueue = String.format(defaultOutputQueue, name);
        this.errorQueue = String.format(defaultErrorQueue, name);
    }

    public String getName() {
        return name;
    }

    public String getExchangeName() {
        return exchange;
    }

    public TopicExchange getExchange() {
        return new TopicExchange(exchange);
    }

    public String getInputQueueName() {
        return inputQueue;
    }

    public Queue getInputQueue() {
        return new Queue(inputQueue);
    }

    public String getOutputQueueName() {
        return outputQueue;
    }

    public Queue getOutputQueue() {
        return new Queue(outputQueue);
    }

    public String getErrorQueueName() {
        return errorQueue;
    }

    public Queue getErrorQueue() {
        return new Queue(errorQueue);
    }

    public Queue getQueue(EventType type) {
        if (type == EventType.INPUT) {
            return getInputQueue();
        } else if (type == EventType.OUTPUT) {
            return getOutputQueue();
        } else {
            return getErrorQueue();
        }
    }
}

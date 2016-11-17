package no.fint.events;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

class Organization {
    private final String name;

    private String exchange;
    private String downstreamQueue;
    private String upstreamQueue;
    private String errorQueue;

    public Organization(String name, String defaultDownstreamQueue, String defaultUpstreamQueue, String defaultErrorQueue) {
        this.name = name;
        this.exchange = name;
        this.downstreamQueue = String.format(defaultDownstreamQueue, name);
        this.upstreamQueue = String.format(defaultUpstreamQueue, name);
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

    public String getDownstreamQueueName() {
        return downstreamQueue;
    }

    public Queue getDownstreamQueue() {
        return new Queue(downstreamQueue);
    }

    public String getUpstreamQueueName() {
        return upstreamQueue;
    }

    public Queue getUpstreamQueue() {
        return new Queue(upstreamQueue);
    }

    public String getErrorQueueName() {
        return errorQueue;
    }

    public Queue getErrorQueue() {
        return new Queue(errorQueue);
    }

    public Queue getQueue(EventType type) {
        if (type == EventType.DOWNSTREAM) {
            return getDownstreamQueue();
        } else if (type == EventType.UPSTREAM) {
            return getUpstreamQueue();
        } else {
            return getErrorQueue();
        }
    }
}

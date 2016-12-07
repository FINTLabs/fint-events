package no.fint.events.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

public class EventsHeaderAndBodyListener extends MessageListenerAdapter {

    private Message message;

    public EventsHeaderAndBodyListener(Object target, String method) {
        super(target, method);
    }

    @Override
    protected Object extractMessage(Message message) {
        this.message = message;
        return super.extractMessage(message);
    }

    @Override
    protected Object[] buildListenerArguments(Object extractedMessage) {
        return new Object[]{message.getMessageProperties().getHeaders(), message.getBody()};
    }

}

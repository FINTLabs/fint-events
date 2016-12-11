package no.fint.events.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

public class EventsReplyToJsonObjectListener extends MessageListenerAdapter {

    private Message message;

    public EventsReplyToJsonObjectListener(Class<?> responseObject, Object target, String method) {
        super(target, method);

        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        defaultClassMapper.setDefaultType(responseObject);
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
        super.setMessageConverter(jackson2JsonMessageConverter);
    }

    @Override
    protected Object extractMessage(Message message) {
        this.message = message;
        return super.extractMessage(message);
    }

    @Override
    protected Object[] buildListenerArguments(Object extractedMessage) {
        String replyTo = message.getMessageProperties().getReplyTo();
        return new Object[]{replyTo, extractedMessage};
    }
}

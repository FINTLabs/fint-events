package no.fint.events.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Slf4j
public class EventsJsonObjectListener extends MessageListenerAdapter {

    public EventsJsonObjectListener(Class<?> responseObject, Object target, String method) {
        super(target, method);

        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        defaultClassMapper.setDefaultType(responseObject);
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
        super.setMessageConverter(jackson2JsonMessageConverter);
    }
}

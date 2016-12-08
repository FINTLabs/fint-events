package no.fint.events.listeners;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.JsonConverterFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Slf4j
public class EventsJsonObjectListener extends MessageListenerAdapter {

    public EventsJsonObjectListener(Class<?> responseObject, Object target, String method) {
        super(target, method);

        Jackson2JsonMessageConverter jackson2JsonMessageConverter = JsonConverterFactory.create(responseObject);
        super.setMessageConverter(jackson2JsonMessageConverter);
    }
}

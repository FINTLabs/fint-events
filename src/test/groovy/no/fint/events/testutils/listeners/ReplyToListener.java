package no.fint.events.testutils.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.Events;
import no.fint.events.testutils.TestDto;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ReplyToListener {

    @Autowired
    private Events events;

    @Autowired
    private ObjectMapper objectMapper;

    public void onMessage(Message message) throws IOException {
        TestDto testDto = objectMapper.readValue(message.getBody(), TestDto.class);
        log.info("ReplyToListener called: {}", testDto);
        events.send(message.getMessageProperties().getReplyTo(), testDto, TestDto.class);
    }
}

package no.fint.events.testutils.listeners;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import no.fint.events.testutils.TestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplyToListener {

    @Autowired
    private FintEvents fintEvents;

    public void onMessage(String replyTo, TestDto testDto) {
        log.info("ReplyToListener called: {}", testDto);
        fintEvents.reply(replyTo, testDto, TestDto.class);
    }
}

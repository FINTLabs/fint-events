package testclient;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.FintEvents;
import no.fint.events.testutils.TestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestDownstreamSubscriber {

    @Autowired
    private FintEvents fintEvents;

    public void receive(String replyTo, TestDto testDto) {
        if (replyTo == null) {
            log.info("Content: {}", testDto);
        } else {
            log.info("Reply to: {} - content: {}", replyTo, testDto);
            fintEvents.reply(replyTo, testDto, TestDto.class);
        }
    }
}

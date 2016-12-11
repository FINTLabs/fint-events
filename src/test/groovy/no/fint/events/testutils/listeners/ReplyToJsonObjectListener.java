package no.fint.events.testutils.listeners;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.events.testutils.TestDto;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplyToJsonObjectListener {

    @Getter
    private boolean called = false;

    public void onReplyToAndObject(String replyTo, TestDto testDto) {
        log.info("JsonObjectListener called: {} - {}", replyTo, testDto);
        called = true;
    }
}

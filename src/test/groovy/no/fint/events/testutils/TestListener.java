package no.fint.events.testutils;

import lombok.Getter;
import no.fint.events.annotations.FintEventsListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener {
    @Getter
    private TestDto testDto;

    @FintEventsListener
    public void receive(TestDto testDto) {
        this.testDto = testDto;
    }

    public void test(TestDto testDto) {
    }
}

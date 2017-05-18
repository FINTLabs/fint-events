package no.fint.events.testutils;

import no.fint.events.annotations.FintEventListener;
import org.springframework.stereotype.Component;

@Component
public class InvalidTestListener {

    @FintEventListener(type = "invalid-queue-type")
    public void receive(TestDto testDto) {
    }
}

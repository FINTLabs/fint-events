package no.fint.events.testutils;

import no.fint.events.annotations.FintEventListener;
import org.springframework.stereotype.Component;

@Component
public class TestListener3 {

    @FintEventListener
    public void receive(TestDto testDto) {
    }

}

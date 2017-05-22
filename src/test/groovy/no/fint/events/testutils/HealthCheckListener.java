package no.fint.events.testutils;

import no.fint.events.FintEventsHealth;
import no.fint.events.annotations.FintEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckListener {

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @FintEventListener
    public void receive(TestDto testDto) {
        testDto.setName("test234");
        fintEventsHealth.respondHealthCheck("123", testDto);
    }
}

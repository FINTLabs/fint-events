package no.fint.events.testutils;

import no.fint.events.FintRemoteEvent;
import org.springframework.stereotype.Component;

@Component
public class TestRemote implements FintRemoteEvent<TestDto> {
    @Override
    public TestDto request(TestDto value) {
        value.setName("test123");
        return value;
    }
}

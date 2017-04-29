package no.fint.events.testutils;

import no.fint.events.remote.RemoteEvent;
import org.springframework.stereotype.Component;

@Component
public class TestRemote implements RemoteEvent<TestDto> {
    @Override
    public TestDto request(TestDto value) {
        value.setName("test123");
        return value;
    }
}

package no.fint.events.testutils;

import no.fint.events.Health;
import org.springframework.stereotype.Component;

@Component
public class TestHealth implements Health<TestDto> {
    @Override
    public TestDto healthCheck(TestDto value) {
        value.setName("health check");
        return value;
    }
}

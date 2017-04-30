package no.fint.events.testutils;

import no.fint.events.HealthCheck;
import org.springframework.stereotype.Component;

@Component
public class TestHealthCheck implements HealthCheck<TestDto> {
    @Override
    public TestDto check(TestDto value) {
        value.setName("health check");
        return value;
    }
}

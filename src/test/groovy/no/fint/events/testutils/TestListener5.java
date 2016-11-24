package no.fint.events.testutils;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TestListener5 {
    private boolean jsonObjectCalled = false;

    public void test4(TestDto testDto) {
        jsonObjectCalled = true;
    }
}

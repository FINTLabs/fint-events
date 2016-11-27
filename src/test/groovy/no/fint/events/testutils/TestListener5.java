package no.fint.events.testutils;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class TestListener5 {
    @Getter
    private boolean jsonObjectCalled = false;

    public void test4(TestDto testDto) {
        jsonObjectCalled = true;
    }
}

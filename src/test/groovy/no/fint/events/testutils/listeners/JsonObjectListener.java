package no.fint.events.testutils.listeners;

import lombok.Getter;
import no.fint.events.testutils.TestDto;
import org.springframework.stereotype.Component;

@Component
public class JsonObjectListener {

    @Getter
    private boolean called = false;

    public void onObject(TestDto testDto) {
        called = true;
    }
}

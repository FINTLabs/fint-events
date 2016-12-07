package no.fint.events.testutils.listeners;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HeaderAndBodyListener {
    @Getter
    private boolean called = false;

    public void onHeaderAndBody(Map<String, String> headers, byte[] body) {
        called = true;
    }

}

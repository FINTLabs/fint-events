package no.fint.events.testutils;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestListener4 {

    // To test method lookup
    public void headerAndBody(Map<String, Object> header, byte[] body) {
    }
}

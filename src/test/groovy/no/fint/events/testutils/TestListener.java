package no.fint.events.testutils;

import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestListener {
    @Getter
    private boolean headerAndBodyCalled = false;

    @Getter
    private boolean messageCalled = false;

    public void test(Message message) {
        messageCalled = true;
    }

    // To test method lookup
    public void test2(String test) {
    }

    public void test3(Map<String, String> headers, byte[] body) {
        headerAndBodyCalled = true;
    }

}

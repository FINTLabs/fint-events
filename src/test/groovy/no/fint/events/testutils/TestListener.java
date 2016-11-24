package no.fint.events.testutils;

import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
public class TestListener {
    private boolean headerAndBodyCalled = false;
    private boolean messageCalled = false;

    public void test(Message message) {
        messageCalled = true;
    }

    public void test2(String test) {
    }

    public void test3(Map<String, String> headers, byte[] body) {
        headerAndBodyCalled = true;
    }

}

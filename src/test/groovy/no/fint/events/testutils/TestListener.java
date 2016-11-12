package no.fint.events.testutils;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
public class TestListener {

    public void test(Message message) {
    }

    public void test2(String test) {
    }

}

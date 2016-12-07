package no.fint.events.testutils.listeners;

import lombok.Getter;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Getter
    private boolean called = false;

    public void onMessage(Message message) {
        called = true;
    }

}

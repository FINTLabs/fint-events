package no.fint.events.listeners

import no.fint.events.testutils.listeners.MessageListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsMessageListenerSpec extends Specification {

    def "Invoke method with message object"() {
        given:
        MessageListener messageListener = new MessageListener()
        EventsMessageListener listener = new EventsMessageListener(messageListener, "onMessage")

        when:
        listener.onMessage(new Message("test".bytes, new MessageProperties()))

        then:
        messageListener.called
    }
}

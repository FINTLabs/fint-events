package no.fint.events.listeners

import no.fint.events.testutils.TestListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsMessageListenerSpec extends Specification {
    private Message message

    void setup() {
        message = Mock(Message) {
            getMessageProperties() >> Mock(MessageProperties) {
                getHeaders() >> Collections.emptyMap()
            }
        }
    }

    def "Invoke method with message object"() {
        given:
        TestListener testListener = new TestListener()
        EventsMessageListener listener = new EventsMessageListener(testListener, "test")

        when:
        listener.invokeListenerMethod("test", new Object[0], message)

        then:
        testListener.messageCalled
    }
}

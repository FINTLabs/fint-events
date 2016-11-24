package no.fint.events.listeners

import no.fint.events.testutils.TestListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsHeaderAndBodyListenerSpec extends Specification {
    private Message message

    void setup() {
        message = Mock(Message) {
            getMessageProperties() >> Mock(MessageProperties) {
                getHeaders() >> Collections.emptyMap()
            }
        }
    }

    def "Invoke method with header and body"() {
        given:
        TestListener testListener = new TestListener()
        EventsHeaderAndBodyListener listener = new EventsHeaderAndBodyListener(testListener, "test3")

        when:
        listener.invokeListenerMethod("test3", new Object[0], message)

        then:
        testListener.headerAndBodyCalled
    }

}

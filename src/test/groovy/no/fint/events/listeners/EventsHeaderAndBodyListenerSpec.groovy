package no.fint.events.listeners

import no.fint.events.testutils.listeners.HeaderAndBodyListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsHeaderAndBodyListenerSpec extends Specification {

    def "Invoke method with header and body"() {
        given:
        def properties = new MessageProperties()
        properties.setHeader("test1", "test2")

        HeaderAndBodyListener headerAndBodyListener = new HeaderAndBodyListener()
        EventsHeaderAndBodyListener listener = new EventsHeaderAndBodyListener(headerAndBodyListener, "onHeaderAndBody")

        when:
        listener.onMessage(new Message("test3".bytes, properties))

        then:
        headerAndBodyListener.called
    }

}

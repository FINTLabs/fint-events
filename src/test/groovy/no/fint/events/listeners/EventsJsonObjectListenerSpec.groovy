package no.fint.events.listeners

import no.fint.events.testutils.TestDto
import no.fint.events.testutils.listeners.JsonObjectListener
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException
import spock.lang.Specification

class EventsJsonObjectListenerSpec extends Specification {

    def "Invoke method with json object"() {
        given:
        JsonObjectListener jsonObjectListener = new JsonObjectListener()
        EventsJsonObjectListener listener = new EventsJsonObjectListener(TestDto, jsonObjectListener, "onObject")
        def message = MessageBuilder.withBody("{\"value\":\"test123\"}".bytes).setContentType(MessageProperties.CONTENT_TYPE_JSON).build()

        when:
        listener.onMessage(message)

        then:
        jsonObjectListener.called
    }

    def "Throw exception when invoking method with invalid json object"() {
        given:
        JsonObjectListener jsonObjectListener = new JsonObjectListener()
        EventsJsonObjectListener listener = new EventsJsonObjectListener(TestDto, jsonObjectListener, "test4")
        def message = MessageBuilder.withBody("test".bytes).setContentType(MessageProperties.CONTENT_TYPE_JSON).build()

        when:
        listener.onMessage(message)

        then:
        thrown(ListenerExecutionFailedException)
    }
}

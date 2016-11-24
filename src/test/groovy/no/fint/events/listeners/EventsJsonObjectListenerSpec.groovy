package no.fint.events.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener5
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsJsonObjectListenerSpec extends Specification {
    private ObjectMapper objectMapper

    void setup() {
        objectMapper = new ObjectMapper()
    }

    def "Invoke method with json object"() {
        given:
        Message message = Mock(Message) {
            getMessageProperties() >> Mock(MessageProperties) {
                getHeaders() >> Collections.emptyMap()
            }
            getBody() >> objectMapper.writeValueAsString(new TestDto(value: "test"))
        }

        TestListener5 testListener = new TestListener5()
        EventsJsonObjectListener listener = new EventsJsonObjectListener(new ObjectMapper(), TestDto, testListener, "test4")

        when:
        listener.invokeListenerMethod("test4", new Object[0], message)

        then:
        testListener.jsonObjectCalled
    }

    def "Throw exception when invoking method with invalid json object"() {
        given:
        Message message = Mock(Message) {
            getMessageProperties() >> Mock(MessageProperties) {
                getHeaders() >> Collections.emptyMap()
            }
            getBody() >> objectMapper.writeValueAsString("test")
        }

        TestListener5 testListener = new TestListener5()
        EventsJsonObjectListener listener = new EventsJsonObjectListener(new ObjectMapper(), TestDto, testListener, "test4")

        when:
        listener.invokeListenerMethod("test4", new Object[0], message)

        then:
        thrown(IllegalArgumentException)
    }
}

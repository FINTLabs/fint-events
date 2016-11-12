package no.fint.events

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.events.testutils.TestDto
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import spock.lang.Specification

class FintEventsSpec extends Specification {
    private FintEvents fintEvents
    private ObjectMapper objectMapper
    private RabbitTemplate rabbitTemplate

    void setup() {
        objectMapper = new ObjectMapper()
        rabbitTemplate = Mock(RabbitTemplate)
        Events events = Mock(Events) {
            rabbitTemplate(_ as String) >> rabbitTemplate
        }

        fintEvents = new FintEvents(objectMapper: objectMapper, events: events)
    }

    def "Create object from json message"() {
        given:
        def testDto = new TestDto(value: "my-value")
        def json = objectMapper.writeValueAsString(testDto)

        when:
        def response = fintEvents.readJson("my-queue", TestDto)

        then:
        1 * rabbitTemplate.receive() >> new Message(json.bytes, null)
        response.value == "my-value"
    }
}

package no.fint.events

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.events.properties.EventsProps
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

        EventsProps eventsProps = Mock(EventsProps) {
            getDefaultDownstreamQueue() >> "downstream"
            getDefaultUpstreamQueue() >> "upstream"
            getDefaultErrorQueue() >> "error"
        }

        fintEvents = new FintEvents(objectMapper: objectMapper, events: events, eventsProps: eventsProps, organizations: [])
    }

    def "Create object from json message"() {
        given:
        def testDto = new TestDto(value: "my-value")
        def json = objectMapper.writeValueAsString(testDto)

        when:
        def response = fintEvents.readJson("my-queue", TestDto)

        then:
        1 * rabbitTemplate.receive() >> new Message(json.bytes, null)
        response.get().value == "my-value"
    }

    def "Return empty optional object if no message is available"() {
        when:
        def response = fintEvents.readJson("my-queue", TestDto)

        then:
        !response.isPresent()
    }

    def "Add and remove organization"() {
        given:
        def orgId = "test.org"

        when:
        fintEvents.addOrganization(orgId)
        def containsOrgAfterAdd = fintEvents.containsOrganization(orgId)
        fintEvents.removeOrganization(orgId)
        def containsOrgAfterRemove = fintEvents.containsOrganization(orgId)

        then:
        containsOrgAfterAdd
        !containsOrgAfterRemove
    }

    def "Get registered orgIds"() {
        given:
        def orgId = "test.org"

        when:
        fintEvents.addOrganization(orgId)
        def orgIds = fintEvents.getRegisteredOrgIds()

        then:
        orgIds.size() == 1
        orgIds[0] == orgId
    }
}

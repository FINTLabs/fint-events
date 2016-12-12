package no.fint.events

import no.fint.events.fintevents.EventType
import no.fint.events.fintevents.FintListeners
import no.fint.events.fintevents.FintOrganisation
import no.fint.events.fintevents.FintOrganisations
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.listeners.StringListener
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import spock.lang.Specification

class FintEventsSpec extends Specification {
    private FintEvents fintEvents
    private Events events
    private FintOrganisations organisations
    private FintOrganisation organisation
    private FintListeners listeners

    void setup() {
        events = Mock(Events)
        organisation = Mock(FintOrganisation) {
            getDownstreamQueueName() >> 'downstream'
            getExchangeName() >> 'exchange'
            getQueue(_ as EventType) >> Mock(Queue) {
                getName() >> 'queue'
            }
        }
        organisations = Mock(FintOrganisations) {
            get(_ as String) >> Optional.of(organisation)
        }

        listeners = Mock(FintListeners)
        fintEvents = new FintEvents(events: events, organisations: organisations, listeners: listeners)
    }

    def "Send and receive with an unknown organisation returns empty response"() {
        when:
        def response = fintEvents.sendAndReceiveDownstream('unknown-org', new TestDto(), TestDto)

        then:
        1 * organisations.get('unknown-org') >> Optional.empty()
        !response.isPresent()
    }

    def "Send and receive to a registered organisation"() {
        given:
        def testDto = new TestDto()

        when:
        def response = fintEvents.sendAndReceiveDownstream('rogfk.no', testDto, TestDto)

        then:
        1 * events.sendAndReceive('exchange', 'queue', testDto, TestDto) >> Optional.of(new TestDto())
        response.isPresent()
    }

    def "Send downstream with type"() {
        given:
        def testDto = new TestDto()

        when:
        fintEvents.sendDownstream('rogfk.no', testDto, TestDto)

        then:
        1 * events.send('downstream', testDto, TestDto)
    }

    def "Send downstream with no default type set"() {
        when:
        fintEvents.sendDownstream('rogfk.no', new TestDto())

        then:
        thrown(IllegalStateException)
    }

    def "Send downstream with default type"() {
        given:
        def testDto = new TestDto()
        fintEvents.setDefaultType(TestDto)

        when:
        fintEvents.sendDownstream('rogfk.no', testDto)

        then:
        1 * events.send('downstream', testDto, TestDto)
    }

    def "Read downstream message"() {
        given:
        def rabbitTemplate = Mock(RabbitTemplate)

        when:
        def response = fintEvents.readDownstream('rogfk.no', TestDto)

        then:
        1 * events.rabbitTemplate(TestDto) >> rabbitTemplate
        1 * rabbitTemplate.receiveAndConvert('queue') >> new TestDto()
        response.isPresent()
    }

    def "Register downstream listener"() {
        when:
        fintEvents.registerDownstreamListener('rogfk.no', StringListener)
        def containsListener = fintEvents.containsDownstreamListener('rogfk.no')

        then:
        1 * listeners.register(StringListener, EventType.DOWNSTREAM, organisation)
        1 * events.containsListener('queue') >> true
        containsListener
    }

    def "Send reply message"() {
        given:
        def testDto = new TestDto()
        fintEvents.setDefaultType(TestDto)

        when:
        fintEvents.reply('reply-to-address', testDto)

        then:
        1 * events.send('reply-to-address', testDto, TestDto)
    }

}

package no.fint.events

import no.fint.events.fintevents.FintListeners
import no.fint.events.fintevents.FintOrganisation
import no.fint.events.fintevents.FintOrganisations
import no.fint.events.testutils.TestDto
import spock.lang.Ignore
import spock.lang.Specification

class FintEventsSpec extends Specification {
    private FintEvents fintEvents
    private Events events
    private FintOrganisations organisations
    private FintListeners listeners

    void setup() {
        events = Mock(Events)
        organisations = Mock(FintOrganisations)
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

    @Ignore
    def "Send and receive to a registered organisation"() {
        when:
        def response = fintEvents.sendAndReceiveDownstream('rogfk.no', new TestDto(), TestDto)

        then:
        1 * organisations.get('rogfk.no') >> Optional.of(new FintOrganisation('rogfk.no', 'downstream', 'upstream', 'error'))
        1 * events.sendAndReceive('rogfk.no', '')
    }
}

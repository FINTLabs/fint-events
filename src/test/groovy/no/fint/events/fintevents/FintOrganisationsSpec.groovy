package no.fint.events.fintevents

import no.fint.events.Events
import no.fint.events.properties.EventsProps
import spock.lang.Specification

class FintOrganisationsSpec extends Specification {
    private FintOrganisations organisations
    private Events events

    void setup() {
        events = Mock(Events)

        EventsProps eventsProps = Mock(EventsProps) {
            getDefaultDownstreamQueue() >> 'downstream'
            getDefaultUpstreamQueue() >> 'upstream'
            getDefaultUndeliveredQueue() >> 'undelivered'
            getOrganizations() >> ['rogfk.no', 'hfk.no', 'vaf.no']
        }

        organisations = new FintOrganisations(eventsProps: eventsProps, events: events)
        organisations.init()
    }

    def "Add and remove organisation"() {
        given:
        def orgId = 'test.org'

        when:
        organisations.add(orgId)
        def containsOrgAfterAdd = organisations.contains(orgId)
        organisations.remove(orgId)
        def containsOrgAfterRemove = organisations.contains(orgId)

        then:
        containsOrgAfterAdd
        !containsOrgAfterRemove
    }

    def "Get registered orgIds"() {
        when:
        def orgIds = organisations.getRegisteredOrgIds()

        then:
        orgIds.size() == 3
    }

    def "Delete default organisation queues"() {
        when:
        organisations.deleteDefaultQueues()

        then:
        3 * events.deleteQueues(_, _)
    }

    def "Get organisation object based on orgId"() {
        when:
        def org = organisations.get('hfk.no')

        then:
        org != null
    }

    def "Get all organisations"() {
        when:
        def orgs = organisations.getAll()

        then:
        orgs.size() == 3
    }

}

package no.fint.events.fintevents

import spock.lang.Specification

class FintOrganisationSpec extends Specification {
    private FintOrganisation organisation

    void setup() {
        organisation = new FintOrganisation("hfk.no", "%s.input", "%s.output", "%s.undelivered")
    }

    def "Get input queue by type"() {
        when:
        def queue = organisation.getQueue(EventType.DOWNSTREAM)

        then:
        queue.getName() == "hfk.no.input"
    }

    def "Get output queue by type"() {
        when:
        def queue = organisation.getQueue(EventType.UPSTREAM)

        then:
        queue.getName() == "hfk.no.output"
    }

    def "Get undelivered queue by type"() {
        when:
        def queue = organisation.getQueue(EventType.UNDELIVERED)

        then:
        queue.getName() == "hfk.no.undelivered"
    }
}

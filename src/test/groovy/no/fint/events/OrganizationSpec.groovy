package no.fint.events

import spock.lang.Specification

class OrganizationSpec extends Specification {
    private Organization organization

    void setup() {
        organization = new Organization("hfk.no", "%s.input", "%s.output", "%s.error")
    }

    def "Get input queue by type"() {
        when:
        def queue = organization.getQueue(EventType.DOWNSTREAM)

        then:
        queue.getName() == "hfk.no.input"
    }

    def "Get output queue by type"() {
        when:
        def queue = organization.getQueue(EventType.UPSTREAM)

        then:
        queue.getName() == "hfk.no.output"
    }

    def "Get error queue by type"() {
        when:
        def queue = organization.getQueue(EventType.ERROR)

        then:
        queue.getName() == "hfk.no.error"
    }
}

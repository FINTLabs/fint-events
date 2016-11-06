package no.fint.events

import spock.lang.Specification

class OrganizationSpec extends Specification {
    private Organization organization

    void setup() {
        organization = new Organization("hfk.no", "%s.input", "%s.output", "%s.error")
    }

    def "Get input queue by type"() {
        when:
        def queue = organization.getQueue(EventType.INPUT)

        then:
        queue.getName() == "hfk.no.input"
    }
}

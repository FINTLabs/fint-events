package no.fint.events

import no.fint.events.testutils.TestApplication
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestRemote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class FintEventsRemoteSpec extends Specification {

    @Autowired
    private FintEventsRemote fintEventsRemoteService

    def "Register server and client, and call the request method"() {
        when:
        def client = fintEventsRemoteService.registerClient()
        fintEventsRemoteService.registerServer(TestRemote)
        def response = client.request(new TestDto(name: 'test'))

        then:
        response.name == 'test123'
    }

}

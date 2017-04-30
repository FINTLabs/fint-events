package no.fint.events

import no.fint.events.testutils.TestApplication
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestHealthCheck
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class FintEventsHealthCheckSpec extends Specification {

    @Autowired
    private FintEventsHealth fintEventsHealth

    def "Register server and client, and call the healthCheck method"() {
        when:
        def client = fintEventsHealth.registerClient()
        fintEventsHealth.registerServer(TestHealthCheck)
        def response = client.check(new TestDto(name: 'test'))

        then:
        response.name == 'health check'
    }

}

package no.fint.events

import no.fint.events.config.FintEventsProps
import no.fint.events.testutils.TestDto
import org.redisson.api.RBlockingQueue
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class FintEventsHealthSpec extends Specification {
    private FintEventsHealth fintEventsHealth
    private FintEvents fintEvents
    private FintEventsProps props
    private RBlockingQueue tempQueue

    void setup() {
        tempQueue = Mock(RBlockingQueue)
        fintEvents = Mock(FintEvents) {
            getTempQueue(_ as String) >> tempQueue
        }
        props = Mock(FintEventsProps) {
            getHealthCheckTimeout() >> 120
        }
        fintEventsHealth = new FintEventsHealth(fintEvents: fintEvents, props: props)
    }

    def "Return null poll method throws InterruptedException"() {
        given:
        def testDto = new TestDto()

        when:
        def response = fintEventsHealth.sendHealthCheck('rogfk.no', '123', testDto)

        then:
        1 * fintEvents.sendDownstream('rogfk.no', testDto)
        1 * tempQueue.poll(120, TimeUnit.SECONDS) >> { throw new InterruptedException('test exception') }
        response == null
    }

    def "Return health check response"() {
        given:
        def testDto = new TestDto()

        when:
        def response = fintEventsHealth.sendHealthCheck('rogfk.no', '123', testDto)

        then:
        1 * fintEvents.sendDownstream('rogfk.no', testDto)
        1 * tempQueue.poll(120, TimeUnit.SECONDS) >> new TestDto(name: 'test123')
        response.name == 'test123'
    }
}

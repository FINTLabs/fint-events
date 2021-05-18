package no.fint.events

import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Ignore
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = TestApplication)
class FintEventsSpec extends Specification {

    @Autowired
    private FintEvents fintEvents

    private Event event

    void setup() {
        event = new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client')
        fintEvents.clearListeners()
    }

    def "Initialize fint events"() {
        expect:
        fintEvents != null
    }

    def "Register downstream listener and send event"() {
        given:
        def latch = new CountDownLatch(1)

        when:
        fintEvents.registerDownstreamListener('rfk.no', { e -> latch.countDown() } as FintEventListener)
        fintEvents.sendDownstream(event)

        then:
        latch.await(2, TimeUnit.SECONDS)
    }

    def "Register upstream listener and send event"() {
        given:
        def latch = new CountDownLatch(1)

        when:
        fintEvents.registerUpstreamListener('rfk.no', { e -> latch.countDown() } as FintEventListener)
        fintEvents.sendUpstream(event)

        then:
        latch.await(2, TimeUnit.SECONDS)
    }

    def "Register upstream system listener and sent system event"() {
        given:
        def latch = new CountDownLatch(1)
        event.orgId = ''

        when:
        fintEvents.registerUpstreamSystemListener({ e -> latch.countDown() })
        fintEvents.sendUpstream(event)

        then:
        latch.await(2, TimeUnit.SECONDS)
    }
}

package no.fint.events

import no.fint.events.testutils.TestApplication
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener
import no.fint.events.testutils.TestListener2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.BlockingQueue

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class FintEventsIntegrationSpec extends Specification {
    @Autowired
    private FintEvents fintEvents

    @Autowired
    private TestListener testListener

    @Autowired
    private TestListener2 testListener2

    def "Create reddison client"() {
        when:
        def client = fintEvents.createRedissonClient()
        client.getMap('test')

        then:
        noExceptionThrown()
    }

    def "Get blocking queue"() {
        when:
        BlockingQueue<TestDto> queue = fintEvents.getQueue('test-queue')

        then:
        queue != null
        queue.size() == 0
    }

    def "Register listener and read message from queue"() {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.02, factor: 1.25)
        fintEvents.getQueue('test-listener-queue').offer(new TestDto(name: 'test123'))
        fintEvents.getQueue('test-listener-queue').offer(new TestDto(name: 'test234'))

        when:
        fintEvents.registerListener('test-listener-queue', TestListener)
        fintEvents.registerListener('test-listener-queue', TestListener2)

        then:
        conditions.eventually {
            assert testListener.testDto != null
            assert testListener.testDto.name == 'test123'
            assert testListener2.testDto != null
            assert testListener2.testDto.name == 'test234'
        }
    }

}

package no.fint.events

import no.fint.events.config.FintEventsProps
import no.fint.events.controller.FintEventsController
import no.fint.events.queue.FintEventsQueue
import no.fint.events.queue.QueueName
import no.fint.events.testutils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.BlockingQueue

@SpringBootTest(classes = TestApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FintEventsIntegrationSpec extends Specification {

    @Autowired
    private FintEvents fintEvents

    @Autowired
    private FintEventsController fintEventsController

    @Autowired
    private FintEventsHealth fintEventsHealth

    @Autowired
    private FintEventsProps props

    @Autowired
    private FintEventsQueue fintEventsQueue

    @Autowired
    private TestReconnectedEventListener testReconnectedEventListener

    @Autowired
    private RestTemplate restTemplate

    @LocalServerPort
    private int port

    @Autowired
    private TestListener testListener

    @Autowired
    private TestListener2 testListener2

    def "Get default queue names"() {
        when:
        def downstream = fintEventsQueue.getDownstreamQueueName(QueueName.with('rogfk.no'))
        def upstream = fintEventsQueue.getUpstreamQueueName(QueueName.with('rogfk.no'))

        then:
        downstream == 'downstream_default_local_rogfk.no'
        upstream == 'upstream_default_local_rogfk.no'
    }

    def "Get redisson client"() {
        when:
        def client = fintEvents.getClient()

        then:
        client != null
    }

    def "Reconnect to redisson"() {
        when:
        fintEvents.reconnect()
        def client = fintEvents.getClient()

        then:
        client != null
        testReconnectedEventListener.reconnected
    }

    def "Get blocking queue"() {
        when:
        BlockingQueue<TestDto> queue = fintEvents.getQueue('test-queue')

        then:
        queue != null
        queue.size() == 0
    }

    def "Return true and delete temporary queues, keep regular queues"() {
        given:
        fintEvents.getQueue('my-queue').add('test123')
        fintEvents.getTempQueue('queue1').add('test234')
        fintEvents.getTempQueue('queue2').add('test345')

        when:
        def deleted = fintEvents.deleteTempQueues()

        then:
        fintEvents.getQueue('my-queue').size() == 1
        deleted
    }

    def "Return true when trying to delete temporary queues, but no queues are found"() {
        when:
        def deleted = fintEvents.deleteTempQueues()

        then:
        deleted
    }

    def "Return empty response when no events are added to the queue"() {
        when:
        def response = restTemplate.getForEntity("http://localhost:${port}/fint-events/queues/test-queue", Map)
        def body = response.getBody()

        then:
        response.statusCode == HttpStatus.OK
        body.keySet()[0] == 'size'
        body.values()[0] == '0'
        body.keySet()[1] == 'value'
        body.values()[1] == ''
    }

    def "Return size and nextValue when there are events in the queue"() {
        given:
        fintEvents.getQueue('test-queue').offer(new TestDto(name: 'testing'))

        when:
        def response = restTemplate.getForEntity("http://localhost:${port}/fint-events/queues/test-queue", Map)
        def body = response.getBody()

        then:
        response.statusCode == HttpStatus.OK
        body.keySet()[0] == 'size'
        body.values()[0] == '1'
        body.keySet()[1] == 'value'
        body.values()[1] == 'TestDto(name=testing)'
    }

    def "Initialize FintEventsController when endpoint is enabled"() {
        when:
        def controllerEnabled = (fintEventsController != null)

        then:
        controllerEnabled
    }

    def "Send and receive health check"() {
        when:
        fintEvents.registerDownstreamListener(HealthCheckListener, 'rogfk.no')
        def response = fintEventsHealth.sendHealthCheck('rogfk.no', '123', new TestDto(name: 'test123'))

        then:
        response.name == 'test234'
    }

    def "Register listener and read message from queue"() {
        given:
        fintEvents.unregisterAllListeners()
        def conditions = new PollingConditions(timeout: 15, initialDelay: 0.02, factor: 1.25)
        fintEvents.getQueue('test-listener-queue').offer(new TestDto(name: 'test123'))
        fintEvents.getQueue('test-listener-queue').offer(new TestDto(name: 'test234'))

        when:
        def listenerId1 = fintEvents.registerListener('test-listener-queue', TestListener)
        def listenerId2 = fintEvents.registerListener('test-listener-queue', TestListener2)

        then:
        conditions.eventually {
            assert testListener.testDto != null
            assert testListener.testDto.name == 'test123'
            assert listenerId1.get() != null
            assert testListener2.testDto != null
            assert testListener2.testDto.name == 'test234'
            assert listenerId2.get() != null
        }
    }

    def "Register and unregister listener"() {
        given:
        fintEvents.unregisterAllListeners()

        when:
        def listenerId = fintEvents.registerListener('test-listener-queue', TestListener)
        def unregistered = fintEvents.unregisterListener(listenerId.get())

        then:
        unregistered
        fintEvents.listeners.size() == 0
        listenerId.get() != null
    }
}

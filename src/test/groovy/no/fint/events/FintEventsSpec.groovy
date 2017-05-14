package no.fint.events

import no.fint.events.config.FintEventsProps
import no.fint.events.config.FintEventsScheduling
import no.fint.events.listener.Listener
import no.fint.events.queue.FintEventsQueue
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener
import org.redisson.api.RBlockingQueue
import org.redisson.api.RedissonClient
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class FintEventsSpec extends Specification {
    private FintEvents fintEvents
    private RedissonClient client
    private FintEventsScheduling scheduling
    private FintEventsQueue fintQueue
    private ApplicationContext applicationContext

    void setup() {
        client = Mock(RedissonClient)
        scheduling = Mock(FintEventsScheduling)
        applicationContext = Mock(ApplicationContext)
        def props = new FintEventsProps(env: 'local', component: 'default',
                defaultDownstreamQueue: 'downstream_{env}_{component}_{orgId}',
                defaultUpstreamQueue: 'upstream_{env}_{component}_{orgId}')
        fintQueue = new FintEventsQueue(props: props)
        fintEvents = new FintEvents(
                client: client,
                props: props,
                scheduling: scheduling,
                fintQueue: fintQueue,
                applicationContext: applicationContext
        )
    }

    def "Temporary queue will not be stored in FintEvents component"() {
        when:
        def queue = fintEvents.getTempQueue('my-queue')

        then:
        1 * client.getBlockingQueue('my-queue') >> Mock(RBlockingQueue)
        queue != null
        fintEvents.getQueues().size() == 0
    }

    def "Get downstream queue"() {
        when:
        def downstream = fintEvents.getDownstream('rogfk.no')

        then:
        1 * client.getBlockingQueue(downstreamQueueName('rogfk.no')) >> Mock(RBlockingQueue)
        downstream != null
        fintEvents.getQueues()[0] == downstreamQueueName('rogfk.no')
    }

    def "Get upstream queue"() {
        when:
        def upstream = fintEvents.getUpstream('rogfk.no')

        then:
        1 * client.getBlockingQueue(upstreamQueueName('rogfk.no')) >> Mock(RBlockingQueue)
        upstream != null
        fintEvents.getQueues()[0] == upstreamQueueName('rogfk.no')
    }

    def "Send object to queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.send('test-queue', testDto)

        then:
        1 * client.getBlockingQueue('test-queue') >> queue
        1 * queue.offer(testDto)
    }

    def "Send object to downstream queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.sendDownstream('rogfk.no', testDto)

        then:
        1 * client.getBlockingQueue(downstreamQueueName('rogfk.no')) >> queue
        1 * queue.offer(testDto)
    }

    def "Send object to upstream queue"() {
        given:
        def queue = Mock(RBlockingQueue)
        def testDto = new TestDto()

        when:
        fintEvents.sendUpstream('rogfk.no', testDto)

        then:
        1 * client.getBlockingQueue(upstreamQueueName('rogfk.no')) >> queue
        1 * queue.offer(testDto)
    }

    def "Register listener"() {
        when:
        fintEvents.registerListener('test-listener-queue', TestListener)

        then:
        1 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue('test-listener-queue')
        1 * scheduling.register(_ as Listener)
        fintEvents.listeners.size() == 1
        fintEvents.listeners.keySet()[0] == 'test-listener-queue'
        fintEvents.listeners.values()[0] > 0L
    }

    def "Register downstream listener"() {
        when:
        fintEvents.registerDownstreamListener(TestListener, 'rogfk.no', 'hfk.no')

        then:
        2 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue(downstreamQueueName('rogfk.no'))
        1 * client.getBlockingQueue(downstreamQueueName('hfk.no'))
        2 * scheduling.register(_ as Listener)
        fintEvents.listeners.size() == 2
        fintEvents.listeners.keySet().contains(downstreamQueueName('rogfk.no') as String)
        fintEvents.listeners.keySet().contains(downstreamQueueName('hfk.no') as String)
        fintEvents.listeners.values()[0] > 0L
        fintEvents.listeners.values()[1] > 0L
    }

    def "Register upstream listener"() {
        when:
        fintEvents.registerUpstreamListener(TestListener, 'rogfk.no', 'hfk.no')

        then:
        2 * applicationContext.getBean(TestListener) >> new TestListener()
        1 * client.getBlockingQueue(upstreamQueueName('rogfk.no'))
        1 * client.getBlockingQueue(upstreamQueueName('hfk.no'))
        2 * scheduling.register(_ as Listener)
        fintEvents.listeners.size() == 2
        fintEvents.listeners.keySet().contains(upstreamQueueName('rogfk.no') as String)
        fintEvents.listeners.keySet().contains(upstreamQueueName('hfk.no') as String)
        fintEvents.listeners.values()[0] > 0L
        fintEvents.listeners.values()[1] > 0L
    }

    def "Shutdown redisson client"() {
        when:
        fintEvents.shutdown()

        then:
        1 * client.shutdown()
    }

    def downstreamQueueName(def orgId) {
        return "downstream_local_default_${orgId}"
    }

    def upstreamQueueName(def orgId) {
        return "upstream_local_default_${orgId}"
    }
}

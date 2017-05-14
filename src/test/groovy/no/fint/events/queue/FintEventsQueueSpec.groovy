package no.fint.events.queue

import no.fint.events.config.FintEventsProps
import spock.lang.Specification

class FintEventsQueueSpec extends Specification {
    private FintEventsQueue fintEventQueue
    private FintEventsProps props

    void setup() {
        props = Mock(FintEventsProps)
        fintEventQueue = new FintEventsQueue(props: props)
    }

    def "Get downstream queue name"() {
        when:
        def queueName = fintEventQueue.getDownstreamQueueName(QueueName.with('local', 'default', 'rogfk.no'))

        then:
        1 * props.getDefaultDownstreamQueue() >> 'downstream_{env}_{component}_{orgId}'
        queueName == 'downstream_local_default_rogfk.no'
    }

    def "Get upstream queue name"() {
        when:
        def queueName = fintEventQueue.getUpstreamQueueName(QueueName.with('local', 'default', 'rogfk.no'))

        then:
        1 * props.getDefaultUpstreamQueue() >> 'upstream_{env}_{component}_{orgId}'
        queueName == 'upstream_local_default_rogfk.no'
    }
}

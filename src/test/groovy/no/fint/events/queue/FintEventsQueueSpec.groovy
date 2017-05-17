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
        1 * props.getDefaultDownstreamQueue() >> 'downstream_{component}_{env}_{orgId}'
        queueName == 'downstream_default_local_rogfk.no'
    }

    def "Get upstream queue name"() {
        when:
        def queueName = fintEventQueue.getUpstreamQueueName(QueueName.with('local', 'default', 'rogfk.no'))

        then:
        1 * props.getDefaultUpstreamQueue() >> 'upstream_{component}_{env}_{orgId}'
        queueName == 'upstream_default_local_rogfk.no'
    }
}

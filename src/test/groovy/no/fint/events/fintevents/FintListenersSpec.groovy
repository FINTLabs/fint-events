package no.fint.events.fintevents

import no.fint.events.Events
import no.fint.events.listeners.EventsMessageListener
import no.fint.events.properties.ListenerProps
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import spock.lang.Specification

class FintListenersSpec extends Specification {
    private FintListeners listeners
    private Events events
    private ListenerProps listenerProps

    void setup() {
        events = Mock(Events) {
            rabbitTemplate() >> Mock(RabbitTemplate)
        }
        listenerProps = Mock(ListenerProps)

        listeners = new FintListeners(events: events, listenerProps: listenerProps)
    }

    def "Register event listener with retry and set acknowledge mode"() {
        when:
        listeners.register(EventsMessageListener, EventType.DOWNSTREAM, new FintOrganisation('rogfk.no', 'downstream', 'upstream', 'undelivered'))

        then:
        1 * events.registerUnstartedListener(_ as TopicExchange, _ as Queue, _ as Class) >> Optional.of(Mock(SimpleMessageListenerContainer))
        1 * listenerProps.retryMaxAttempts >> 1
        1 * listenerProps.retryInitialInterval >> 1.0
        1 * listenerProps.retryMultiplier >> 1.5
        1 * listenerProps.retryMaxInterval >> 100
        1 * listenerProps.acknowledgeMode >> 'AUTO'
    }
}

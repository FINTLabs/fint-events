package no.fint.events.internal

import com.hazelcast.core.ITopic
import com.hazelcast.core.Message
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.events.FintEventListener
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EventDispatcherSpec extends Specification {

    def "Incoming event gets dispatched to event listener"() {
        given:
        def topic = Mock(ITopic)
        def message = Mock(Message)
        def latch = new CountDownLatch(1)

        when:
        def eventDispatcher = new EventDispatcher(topic)
        eventDispatcher.registerListener('rfk.no', { event -> latch.countDown() } as FintEventListener)
        eventDispatcher.onMessage(message)

        then:
        latch.await(2, TimeUnit.SECONDS)
        1 * message.messageObject >> new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client')
        1 * topic.addMessageListener(_ as EventDispatcher)
    }

}

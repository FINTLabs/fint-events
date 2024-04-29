package no.fint.events.internal

import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fintlabs.events.FintEventListener
import no.fintlabs.events.internal.EventDispatcher
import spock.lang.Specification

import java.util.concurrent.*

class EventDispatcherSpec extends Specification {
    private EventDispatcher eventDispatcher
    private BlockingQueue<Event> queue

    void setup() {
        queue = new SynchronousQueue<Event>()
        eventDispatcher = new EventDispatcher(queue)
    }

    def "Incoming event gets dispatched to event listener"() {
        given:
        def latch = new CountDownLatch(1)
        eventDispatcher.registerListener('rfk.no', { event -> latch.countDown() } as FintEventListener)

        when:
        Executors.newSingleThreadExecutor().execute(eventDispatcher)
        queue.put(new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client'))

        then:
        latch.await(2, TimeUnit.SECONDS)
    }

    def "Do not start two dispatchers"() {
        given:
        def latch = new CountDownLatch(1)
        eventDispatcher.registerListener('rfk.no', { event -> latch.countDown() } as FintEventListener)
        def executorService = Executors.newFixedThreadPool(2)

        when:
        executorService.execute(eventDispatcher)
        executorService.execute(eventDispatcher)
        queue.put(new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client'))

        then:
        latch.await(2, TimeUnit.SECONDS)
    }
}

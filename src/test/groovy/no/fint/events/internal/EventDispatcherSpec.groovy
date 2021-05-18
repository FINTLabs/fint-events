package no.fint.events.internal

import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.events.FintEventListener
import spock.lang.Specification

import java.util.concurrent.*

class EventDispatcherSpec extends Specification {
    private EventDispatcher eventDispatcher
    private BlockingQueue<Event> queue
    private ExecutorService executorService

    void setup() {
        queue = new SynchronousQueue<Event>()
        executorService = Executors.newFixedThreadPool(2)
        eventDispatcher = new EventDispatcher(queue, executorService)
    }

    def "Incoming event gets dispatched to event listener"() {
        given:
        def latch = new CountDownLatch(1)
        eventDispatcher.registerListener({ event -> latch.countDown() } as FintEventListener)

        when:
        Executors.newSingleThreadExecutor().execute(eventDispatcher)
        queue.put(new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client'))

        then:
        latch.await(2, TimeUnit.SECONDS)
    }

    def "Do not start two dispatchers"() {
        given:
        def latch = new CountDownLatch(1)
        eventDispatcher.registerListener({ event -> latch.countDown() } as FintEventListener)

        when:
        executorService.execute(eventDispatcher)
        executorService.execute(eventDispatcher)
        queue.put(new Event('rfk.no', 'test-source', DefaultActions.HEALTH, 'test-client'))

        then:
        latch.await(2, TimeUnit.SECONDS)
    }
}

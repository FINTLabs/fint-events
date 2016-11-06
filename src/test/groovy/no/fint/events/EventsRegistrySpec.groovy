package no.fint.events

import no.fint.events.testutils.TestApplication
import no.fint.events.testutils.TestListener
import no.fint.events.testutils.TestListener2
import no.fint.events.testutils.TestListener3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ActiveProfiles("norabbitmq")
@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class EventsRegistrySpec extends Specification {

    @Autowired
    private EventsRegistry eventsRegistry

    def "Add and remove listener"() {
        given:
        def queue = "test-queue"
        def listener = TestListener

        when:
        eventsRegistry.add(queue, listener)
        def containsListener = eventsRegistry.containsListener(queue)
        eventsRegistry.shutdown()

        then:
        containsListener
    }

    def "Get message listener method"() {
        when:
        def method = eventsRegistry.getMessageListenerMethod(TestListener)

        then:
        method.isPresent()
        method.get().getName() == "test"
    }

    def "Get public method"() {
        when:
        def method = eventsRegistry.getPublicMethod(TestListener2)

        then:
        method.isPresent()
        method.get().getName() == "test2"
    }

    def "No method returned when no listener method is found"() {
        when:
        def method = eventsRegistry.getPublicMethod(TestListener3)

        then:
        !method.isPresent()
    }
}

package no.fint.events

import no.fint.events.testutils.*
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
@TestPropertySource(properties = "fint.events.test-mode=true")
class EventsRegistrySpec extends Specification {

    @Autowired
    private EventsRegistry eventsRegistry

    def "Add listener and call shutdown"() {
        given:
        def queue = "test-queue"

        when:
        eventsRegistry.add(queue, TestListener)
        def containsListener = eventsRegistry.containsListener("test-queue")
        eventsRegistry.shutdown()

        then:
        containsListener
    }

    def "Add and remove listener with header and body arguments"() {
        given:
        def queue = "test-queue2"

        when:
        eventsRegistry.add(queue, TestListener4)
        def containsListener = eventsRegistry.containsListener("test-queue2")
        eventsRegistry.close("test-queue2")

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

    def "Get header and body listener method"() {
        when:
        def method = eventsRegistry.getHeaderAndBodyListenerMethod(TestListener)

        then:
        method.isPresent()
        method.get().getName() == "test3"
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

    def "Exception when trying to register non-spring bean"() {
        when:
        eventsRegistry.add("test-queue123", String.class)

        then:
        thrown(NoSuchBeanDefinitionException)
    }
}

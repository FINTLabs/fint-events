package no.fint.events.queue

import no.fint.events.queue.testutils.TestApplication
import no.fint.events.queue.testutils.TestListener
import no.fint.events.queue.testutils.TestListener2
import no.fint.events.queue.testutils.TestListener3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class DynamicQueuesRegistrySpec extends Specification {

    @Autowired
    private DynamicQueuesRegistry dynamicQueuesRegistry

    def "Add and remove listener"() {
        given:
        def queue = "test-queue"
        def listener = TestListener

        when:
        dynamicQueuesRegistry.add(queue, listener)
        def containsListener = dynamicQueuesRegistry.containsListener(queue)
        dynamicQueuesRegistry.shutdown()

        then:
        containsListener
    }

    def "Get message listener method"() {
        when:
        def method = dynamicQueuesRegistry.getMessageListenerMethod(TestListener)

        then:
        method.isPresent()
        method.get().getName() == "test"
    }

    def "Get public method"() {
        when:
        def method = dynamicQueuesRegistry.getPublicMethod(TestListener2)

        then:
        method.isPresent()
        method.get().getName() == "test2"
    }

    def "No method returned when no listener method is found"() {
        when:
        def method = dynamicQueuesRegistry.getPublicMethod(TestListener3)

        then:
        !method.isPresent()
    }
}

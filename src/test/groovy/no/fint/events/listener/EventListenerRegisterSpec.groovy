package no.fint.events.listener

import no.fint.events.FintEvents
import no.fint.events.config.FintEventsProps
import no.fint.events.testutils.*
import org.springframework.context.ApplicationContext
import org.springframework.util.ReflectionUtils
import spock.lang.Specification

class EventListenerRegisterSpec extends Specification {
    private EventListenerRegister eventListenerRegister
    private ApplicationContext applicationContext
    private FintEventsProps props
    private FintEvents fintEvents

    void setup() {
        applicationContext = Mock(ApplicationContext) {
            getBeanDefinitionNames() >> ['testListener'].toArray()
        }
        fintEvents = Mock(FintEvents)
        props = new FintEventsProps(orgIds: ['mock.no'].toArray())
        eventListenerRegister = new EventListenerRegister(fintEvents: fintEvents, props: props)
        eventListenerRegister.setApplicationContext(applicationContext)
    }

    def "Return empty listener class when no exception is thrown in AopUtil"() {
        when:
        def metadata = eventListenerRegister.getListenerMetadata()

        then:
        !metadata.isPresent()
    }

    def "Return event listener"() {
        when:
        def metadata = eventListenerRegister.getListenerMetadata()

        then:
        1 * applicationContext.getBean('testListener') >> new TestListener()
        metadata.isPresent()
        metadata.get().clazz == TestListener
        metadata.get().method.getName() == 'receive'
    }

    def "Return empty if no @FintEventListener is found on bean"() {
        when:
        def metadata = eventListenerRegister.getListenerMetadata()

        then:
        1 * applicationContext.getBean('testListener') >> new TestDto()
        !metadata.isPresent()
    }

    def "Return empty if multiple event listener beans are found"() {
        given:
        applicationContext = Mock(ApplicationContext) {
            getBeanDefinitionNames() >> ['testListener1', 'testListener2'].toArray()
        }
        eventListenerRegister.setApplicationContext(applicationContext)

        when:
        def metadata = eventListenerRegister.getListenerMetadata()

        then:
        2 * applicationContext.getBean(_ as String) >> new TestListener()
        !metadata.isPresent()
    }

    def "Register downstream listener"() {
        given:
        def method = ReflectionUtils.findMethod(TestListener, 'receive', TestDto)
        def metadata = new EventListenerMetadata(TestListener, method)

        when:
        eventListenerRegister.registerListener(metadata)

        then:
        1 * fintEvents.registerDownstreamListener(TestListener, 'mock.no')
    }

    def "Register upstream listener"() {
        given:
        def method = ReflectionUtils.findMethod(TestListener2, 'receive', TestDto)
        def metadata = new EventListenerMetadata(TestListener2, method)

        when:
        eventListenerRegister.registerListener(metadata)

        then:
        1 * fintEvents.registerUpstreamListener(TestListener2, 'mock.no')
    }

    def "Do not register listener when queue type is invalid"() {
        given:
        def method = ReflectionUtils.findMethod(InvalidTestListener, 'receive', TestDto)
        def metadata = new EventListenerMetadata(InvalidTestListener, method)

        when:
        eventListenerRegister.registerListener(metadata)

        then:
        0 * fintEvents.registerUpstreamListener(InvalidTestListener, 'mock.no')
    }

    def "Do not register listener when no queue type is configured"() {
        given:
        def method = ReflectionUtils.findMethod(TestListener3, 'receive', TestDto)
        def metadata = new EventListenerMetadata(TestListener3, method)

        when:
        eventListenerRegister.registerListener(metadata)

        then:
        0 * fintEvents.registerUpstreamListener(TestListener3, 'mock.no')
    }
}

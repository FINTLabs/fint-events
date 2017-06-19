package no.fint.events.scheduling

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener
import org.redisson.RedissonShutdownException
import org.redisson.client.RedisException
import org.springframework.util.ReflectionUtils
import spock.lang.Specification

import java.lang.reflect.Method
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class ListenerSpec extends Specification {
    private Method method
    private BlockingQueue queue
    private TestListener testListener
    private Listener listener

    void setup() {
        method = ReflectionUtils.findMethod(TestListener, "receive", TestDto)
        queue = new ArrayBlockingQueue(10)
        testListener = new TestListener()
        listener = new Listener(testListener, method, 'test-queue', queue)
    }

    def "Call run on listener"() {
        when:
        queue.offer(new TestDto(name: 'test123'))
        listener.run()

        then:
        testListener.testDto.name == 'test123'
    }

    def "Log error and do not call listener when input is wrong type"() {
        when:
        queue.offer('test123')
        listener.run()

        then:
        testListener.testDto == null
    }

    def "Log debug message if listener receives exception because of redisson shutdown"() {
        given:
        def exceptionQueue = Mock(ArrayBlockingQueue) {
            take() >> { throw new RedissonShutdownException('test exception') }
        }
        def exceptionListener = new Listener(testListener, method, 'test-queue', exceptionQueue)

        when:
        exceptionListener.run()

        then:
        testListener.testDto == null
    }

    def "Log debug message if listener receives RedisException"() {
        given:
        def exceptionQueue = Mock(ArrayBlockingQueue) {
            take() >> { throw new RedisException('test exception') }
        }
        def exceptionListener = new Listener(testListener, method, 'test-queue', exceptionQueue)

        when:
        exceptionListener.run()

        then:
        testListener.testDto == null
    }

    def "Serialize Listener object to json"() {
        when:
        def listener = new Listener(testListener, method, 'test-queue', Mock(ArrayBlockingQueue))
        def json = new ObjectMapper().writeValueAsString(listener)

        then:
        json != null
    }
}

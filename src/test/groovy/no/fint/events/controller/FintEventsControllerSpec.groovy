package no.fint.events.controller

import no.fint.events.FintEvents
import no.fint.events.config.RedissonConfig
import no.fint.events.scheduling.Listener
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.TestListener
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.BlockingQueue

class FintEventsControllerSpec extends MockMvcSpecification {
    private FintEventsController controller
    private FintEvents fintEvents
    private RedissonConfig redissonConfig
    private MockMvc mockMvc

    void setup() {
        fintEvents = Mock(FintEvents)
        redissonConfig = Mock(RedissonConfig)
        controller = new FintEventsController(fintEvents: fintEvents, redissonConfig: redissonConfig)
        mockMvc = standaloneSetup(controller)
    }

    def "Return all queues"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues'))

        then:
        1 * fintEvents.getQueues() >> ['test-queue']
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(1)))
    }

    def "Get next value in  queue, return size and content of queue"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues/test-queue'))

        then:
        1 * fintEvents.getQueue('test-queue') >> Mock(BlockingQueue) {
            size() >> 1
            peek() >> new TestDto(name: 'test123')
        }
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.size').value(equalTo('1')))
                .andExpect(jsonPath('$.value').value('TestDto(name=test123)'))
    }

    def "Get queue content, return content for index"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues/test-queue').param('index', '0'))

        then:
        1 * fintEvents.getQueue('test-queue') >> Mock(BlockingQueue) {
            size() >> 1
            toArray() >> [new TestDto(name: 'test123')]
        }
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.size').value('1'))
                .andExpect(jsonPath('$.value').value('TestDto(name=test123)'))
    }

    def "Get queue content, return empty if index does not exist"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues/test-queue').param('index', '1'))

        then:
        1 * fintEvents.getQueue('test-queue') >> Mock(BlockingQueue) {
            size() >> 0
            toArray() >> []
        }
        response.andExpect(status().isOk())
    }

    def "Get registered event listeners"() {
        when:
        def response = mockMvc.perform(get('/fint-events/listeners'))

        then:
        1 * fintEvents.getListeners() >> [new Listener(object: new TestListener(), queueName: 'test-listener')]
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$[0].queueName').value(equalTo('test-listener')))
                .andExpect(jsonPath('$[0].object').value(equalTo(TestListener.name)))
    }

    def "Get redisson config"() {
        when:
        def response = mockMvc.perform(get('/fint-events/redissonConfig'))

        then:
        1 * redissonConfig.getRedissonJsonConfig() >> ['{}']
        response.andExpect(status().isOk())
    }
}

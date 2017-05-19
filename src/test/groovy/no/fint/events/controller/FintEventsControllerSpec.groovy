package no.fint.events.controller

import no.fint.events.FintEvents
import no.fint.events.testutils.TestDto
import no.fint.test.utils.MockMvcSpecification
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.BlockingQueue

class FintEventsControllerSpec extends MockMvcSpecification {
    private FintEventsController controller
    private FintEvents fintEvents
    private MockMvc mockMvc

    void setup() {
        fintEvents = Mock(FintEvents)
        controller = new FintEventsController(fintEvents: fintEvents)
        mockMvc = standaloneSetup(controller)
    }

    def "Return all queues"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues'))

        then:
        1 * fintEvents.getComponentQueues() >> ['test-queue']
        1 * fintEvents.getQueues() >> ['test-queue']
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.componentQueues', hasSize(1)))
                .andExpect(jsonPath('$.queues', hasSize(1)))
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
        1 * fintEvents.getListeners() >> ['test-listener': 123L]
        response.andExpect(status().isOk())
        .andExpect(jsonPath('$.test-listener').value(equalTo(123)))
    }
}

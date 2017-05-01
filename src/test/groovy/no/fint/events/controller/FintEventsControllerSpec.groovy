package no.fint.events.controller

import com.github.spock.spring.utils.MockMvcSpecification
import no.fint.events.FintEvents
import no.fint.events.config.FintEventsProps
import no.fint.events.testutils.TestDto
import org.springframework.test.web.servlet.MockMvc

import java.util.concurrent.BlockingQueue

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.Matchers.hasSize

class FintEventsControllerSpec extends MockMvcSpecification {
    private FintEventsController controller
    private FintEventsProps props
    private FintEvents fintEvents
    private MockMvc mockMvc

    void setup() {
        props = Mock(FintEventsProps)
        fintEvents = Mock(FintEvents)
        controller = new FintEventsController(fintEvents: fintEvents, props: props)
        mockMvc = standaloneSetup(controller)
    }

    def "Return all queues"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues'))

        then:
        1 * props.getQueueEndpointEnabled() >> 'true'
        1 * fintEvents.getQueues() >> ['test-queue']
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(1)))
    }

    def "Get queue names, return 404 when queue endpoint is disabled"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues'))

        then:
        1 * props.getQueueEndpointEnabled() >> 'false'
        response.andExpect(status().isNotFound())
    }

    def "Get queue, return size and content of queue"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues/test-queue'))

        then:
        1 * props.getQueueEndpointEnabled() >> 'true'
        1 * fintEvents.getQueue('test-queue') >> Mock(BlockingQueue) {
            size() >> 1
            peek() >> new TestDto(name: 'test123')
        }
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.size').value(equalTo('1')))
                .andExpect(jsonPath('$.nextValue').value('TestDto(name=test123)'))
    }

    def "Get queue, return 404 when queue endpoint is disabled"() {
        when:
        def response = mockMvc.perform(get('/fint-events/queues/test-queue'))

        then:
        1 * props.getQueueEndpointEnabled() >> 'false'
        response.andExpect(status().isNotFound())
    }
}

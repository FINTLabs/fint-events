package no.fint.events.e2e

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import no.fint.events.Events
import no.fint.events.FintEvents
import no.fint.events.fintevents.FintOrganisations
import no.fint.events.testutils.TestApplication
import no.fint.events.testutils.TestDto
import no.fint.events.testutils.listeners.JsonObjectListener
import no.fint.events.testutils.listeners.ReplyToListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Requires({ Boolean.valueOf(properties['e2e.enabled']) })
@ContextConfiguration
@TestPropertySource(properties = "fint.rabbitmq.reply-to-timeout=3000")
@SpringBootTest(classes = TestApplication)
class FintEventsIntegrationSpec extends Specification {
    @Shared
    private LocalRabbit localRabbit

    private PollingConditions conditions = new PollingConditions(timeout: 10)

    @Autowired
    private FintOrganisations organisations

    @Autowired
    private FintEvents fintEvents

    @Autowired
    private Events events

    @Autowired
    private JsonObjectListener jsonObjectListener

    void setupSpec() {
        def logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        logger.setLevel(Level.INFO)

        localRabbit = new LocalRabbit()
        localRabbit.start()
    }

    void cleanupSpec() {
        localRabbit.stop()
    }

    void setup() {
        organisations.addOrganisation('rogfk.no')
    }

    void cleanup() {
        def org = organisations.get('rogfk.no')
        events.removeListener(org.get().getExchange(), org.get().getDownstreamQueue())
        organisations.removeOrganisation('rogfk.no')
    }

    def "Send message to message listener"() {
        given:
        def testDto = new TestDto(value: 'testing')

        when:
        fintEvents.registerDownstreamListener('rogfk.no', JsonObjectListener)
        fintEvents.sendDownstream('rogfk.no', testDto, TestDto)

        then:
        conditions.eventually {
            assert jsonObjectListener.called
        }
    }

    def "Send and receive message"() {
        given:
        def testDto = new TestDto(value: 'testing')

        when:
        fintEvents.registerDownstreamListener('rogfk.no', ReplyToListener)
        def response = fintEvents.sendAndReceiveDownstream('rogfk.no', testDto, TestDto)

        then:
        response.isPresent()
    }

    def "Send and receive message with timeout"() {
        given:
        def testDto = new TestDto(value: 'testing')

        when:
        def response = fintEvents.sendAndReceiveDownstream('rogfk.no', testDto, TestDto)

        then:
        !response.isPresent()
    }
}

package no.fint.events

import no.fint.events.config.FintEventsProps
import no.fint.events.testutils.TestHealthCheck
import org.redisson.api.RRemoteService
import org.redisson.api.RedissonClient
import org.redisson.api.RemoteInvocationOptions
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class FintEventsHealthSpec extends Specification {
    private FintEventsHealth fintEventsHealth
    private RRemoteService remoteService
    private ApplicationContext applicationContext
    private FintEventsProps props

    void setup() {
        remoteService = Mock(RRemoteService)
        def fintEvents = Mock(FintEvents) {
            getClient() >> Mock(RedissonClient) {
                getRemoteService() >> remoteService
            }
        }
        applicationContext = Mock(ApplicationContext)
        props = Mock(FintEventsProps)
        fintEventsHealth = new FintEventsHealth(fintEvents: fintEvents, applicationContext: applicationContext, props: props)
    }

    def "Register server"() {
        given:
        TestHealthCheck testHealth = new TestHealthCheck()

        when:
        fintEventsHealth.registerServer(TestHealthCheck)

        then:
        1 * applicationContext.getBean(TestHealthCheck) >> testHealth
        1 * remoteService.register(HealthCheck, testHealth)
    }

    def "Register client"() {
        when:
        fintEventsHealth.init()
        def client = fintEventsHealth.registerClient()

        then:
        1 * remoteService.get(HealthCheck, _ as RemoteInvocationOptions) >> Mock(HealthCheck)
        1 * props.healthCheckTimeout >> 10
        client != null
    }

    def "Deregister client"() {
        given:
        fintEventsHealth.init()
        fintEventsHealth.registerClient()

        when:
        fintEventsHealth.deregisterClient()

        then:
        1 * remoteService.deregister(HealthCheck)
    }

    def "Deregister should not be called when no client is registered"() {
        given:
        fintEventsHealth.init()

        when:
        fintEventsHealth.deregisterClient()

        then:
        0 * remoteService.deregister(HealthCheck)
    }
}

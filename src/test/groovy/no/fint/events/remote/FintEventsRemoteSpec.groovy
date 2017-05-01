package no.fint.events.remote

import no.fint.events.FintEvents
import no.fint.events.testutils.TestRemote
import org.redisson.api.RRemoteService
import org.redisson.api.RedissonClient
import org.redisson.api.RemoteInvocationOptions
import org.springframework.context.ApplicationContext
import spock.lang.Specification

class FintEventsRemoteSpec extends Specification {
    private FintEventsRemote fintEventsRemote
    private RRemoteService remoteService
    private ApplicationContext applicationContext

    void setup() {
        remoteService = Mock(RRemoteService)
        def fintEvents = Mock(FintEvents) {
            getClient() >> Mock(RedissonClient) {
                getRemoteService() >> remoteService
            }
        }
        applicationContext = Mock(ApplicationContext)
        fintEventsRemote = new FintEventsRemote(fintEvents: fintEvents, applicationContext: applicationContext)
    }

    def "Register server"() {
        given:
        TestRemote testRemote = new TestRemote()

        when:
        fintEventsRemote.registerServer(TestRemote)

        then:
        1 * applicationContext.getBean(TestRemote) >> testRemote
        1 * remoteService.register(RemoteEvent, testRemote)
    }

    def "Register client"() {
        when:
        fintEventsRemote.init()
        def client = fintEventsRemote.registerClient()

        then:
        1 * remoteService.get(RemoteEvent, _ as RemoteInvocationOptions) >> Mock(RemoteEvent)
        client != null
    }
}

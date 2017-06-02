package no.fint.events.config

import no.fint.events.FintEvents
import org.redisson.api.NodesGroup
import org.redisson.api.RedissonClient
import org.redisson.client.RedisTimeoutException
import spock.lang.Specification

class RedissonConnectionSpec extends Specification {
    private RedissonConnection connection
    private FintEvents fintEvents
    private RedissonConfig redissonConfig
    private NodesGroup nodesGroup

    void setup() {
        nodesGroup = Mock(NodesGroup)
        fintEvents = Mock(FintEvents) {
            getClient() >> Mock(RedissonClient) {
                getNodesGroup() >> nodesGroup
            }
        }
        redissonConfig = Mock(RedissonConfig) {
            getAutoReconnect() >> 'true'
        }
        connection = new RedissonConnection(fintEvents: fintEvents, redissonConfig: redissonConfig)
    }

    def "Return false when redis nodes answers to ping"() {
        when:
        def connectionLost = connection.connectionLost()

        then:
        1 * nodesGroup.pingAll() >> true
        !connectionLost
    }

    def "Return true when ping request throws exception"() {
        when:
        def connectionLost = connection.connectionLost()

        then:
        1 * nodesGroup.pingAll() >> { throw new RedisTimeoutException('test exception') }
        connectionLost
    }

    def "Set disconnected to true if redis connection is down"() {
        when:
        connection.checkRedisConnection()

        then:
        connection.disconnected.get()
    }
}

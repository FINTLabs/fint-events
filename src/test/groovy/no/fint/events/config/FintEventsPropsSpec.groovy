package no.fint.events.config

import org.redisson.config.Config
import spock.lang.Specification

class FintEventsPropsSpec extends Specification {

    def "Load redisson config when test mode is disabled"() {
        given:
        def redisson = Mock(RedissonConfig)
        def props = new FintEventsProps(redisson: redisson, testMode: 'false')

        when:
        props.init()

        then:
        1 * redisson.getConfig() >> Mock(Config)
        props.getRedissonConfig() != null
    }

    def "Load default redisson config when test mode is enabled"() {
        given:
        def redisson = Mock(RedissonConfig)
        def props = new FintEventsProps(redisson: redisson, testMode: 'true')

        when:
        props.init()

        then:
        1 * redisson.getDefaultConfig() >> Mock(Config)
        props.getRedissonConfig() != null
    }
}

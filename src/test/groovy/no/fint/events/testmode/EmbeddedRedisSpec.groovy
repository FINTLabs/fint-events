package no.fint.events.testmode

import no.fint.events.config.FintEventsProps
import spock.lang.Specification

class EmbeddedRedisSpec extends Specification {

    def "Do not initialize server if test mode is disabled"() {
        given:
        def props = new FintEventsProps(testMode: 'false')
        def embeddedRedis = new EmbeddedRedis(props: props)

        when:
        embeddedRedis.init()
        def started = embeddedRedis.isStarted()

        then:
        !started
    }
}

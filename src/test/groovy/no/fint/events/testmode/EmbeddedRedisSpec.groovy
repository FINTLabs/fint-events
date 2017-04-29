package no.fint.events.testmode

import no.fint.events.config.FintEventsProps
import no.fint.events.testutils.TestApplication
import org.redisson.Redisson
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class EmbeddedRedisSpec extends Specification {

    @Autowired
    public EmbeddedRedis embeddedRedis

    @Autowired
    private FintEventsProps props

    def "Init and shutdown embedded redis"() {
        given:
        Config config = new Config()
        config.useSingleServer().setAddress(props.getRedisAddress())

        when:
        def client = Redisson.create(config)
        def number = client.getAtomicLong("test")
        number.set(123)
        def response = client.getAtomicLong("test").get()
        client.shutdown()
        embeddedRedis.shutdown()

        then:
        noExceptionThrown()
        response == 123L
    }
}

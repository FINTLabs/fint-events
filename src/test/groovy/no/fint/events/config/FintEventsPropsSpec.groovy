package no.fint.events.config

import no.fint.events.testutils.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(classes = TestApplication)
class FintEventsPropsSpec extends Specification {

    @Autowired
    private FintEventsProps props

    def "Verify that the default value of redis-configuration is 'single'"() {
        when:
        def configuration = props.getRedisConfiguration()

        then:
        configuration == RedisConfiguration.SINGLE
    }

}

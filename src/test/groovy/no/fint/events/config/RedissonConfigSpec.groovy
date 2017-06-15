package no.fint.events.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.env.Environment
import spock.lang.Specification

class RedissonConfigSpec extends Specification {
    private RedissonConfig redissonConfig
    private Environment environment

    void setup() {
        environment = Mock(Environment)
        redissonConfig = new RedissonConfig(
                addresses: ['redis://127.0.0.1:6379'] as String[],
                mode: 'SINGLE',
                retryAttempts: 50,
                retryInterval: 3000,
                reconnectionTimeout: 6000,
                timeout: 6000,
                environment: environment,
                dnsMonitoring: 'true'
        )
    }

    def "Load default redisson config when no yml file is found"() {
        when:
        redissonConfig.init()
        def config = redissonConfig.getConfig()

        def configValues = new ObjectMapper().readValue(redissonConfig.redissonJsonConfig, Map)
        def singleServerConfig = configValues['singleServerConfig']

        then:
        1 * environment.getActiveProfiles() >> ['default']
        config != null
        configValues['singleServerConfig'].size() == 6
        singleServerConfig['address'] == 'redis://127.0.0.1:6379'
        singleServerConfig['retryAttempts'] == 50
        singleServerConfig['retryInterval'] == 3000
        singleServerConfig['reconnectionTimeout'] == 6000
        singleServerConfig['timeout'] == 6000
        singleServerConfig['dnsMonitoring'] == true
    }

    def "Create cluster redisson json"() {
        given:
        def redissonConfig = new RedissonConfig(
                addresses: ['redis://127.0.0.1:6379', 'redis://127.0.0.2:6379'] as String[],
                mode: 'CLUSTER',
                retryAttempts: 50,
                retryInterval: 3000,
                reconnectionTimeout: 6000,
                timeout: 6000,
                dnsMonitoring: 'false'
        )

        when:
        redissonConfig.init()
        def configValues = new ObjectMapper().readValue(redissonConfig.redissonJsonConfig, Map)
        def clusterConfig = configValues['clusterServersConfig']

        then:
        configValues['clusterServersConfig'].size() == 5
        clusterConfig['nodeAddresses'] == ['redis://127.0.0.1:6379', 'redis://127.0.0.2:6379'] as String[]
        clusterConfig['retryAttempts'] == 50
        clusterConfig['retryInterval'] == 3000
        clusterConfig['reconnectionTimeout'] == 6000
        clusterConfig['timeout'] == 6000
    }

    def "Read redisson config file"() {
        when:
        redissonConfig.init()
        def config = redissonConfig.getConfig()

        then:
        1 * environment.getActiveProfiles() >> ['test']
        config != null
    }

    def "Get default redisson config"() {
        when:
        redissonConfig.init()
        def config = redissonConfig.getDefaultConfig()

        then:
        config != null
    }

    def "Throw IllegalArgumentException when redisson cannot parse json config"() {
        given:
        def redissonConfig = new RedissonConfig(redissonJsonConfig: 'invalid-json')

        when:
        redissonConfig.getDefaultConfig()

        then:
        thrown(IllegalArgumentException)
    }

}

package no.fint.events.config

import spock.lang.Specification

class RedissonModeSpec extends Specification {

    def "Cluster servers config"() {
        when:
        def single = RedissonMode.CLUSTER

        then:
        single.addressField == 'nodeAddresses'
        single.modeRoot == 'clusterServersConfig'
    }

    def "Replicated servers config"() {
        when:
        def replicated = RedissonMode.REPLICATED

        then:
        replicated.addressField == 'nodeAddresses'
        replicated.modeRoot == 'replicatedServersConfig'
    }

    def "Single server config"() {
        when:
        def single = RedissonMode.SINGLE

        then:
        single.addressField == 'address'
        single.modeRoot == 'singleServerConfig'
    }

    def "Sentinel servers config"() {
        when:
        def sentinel = RedissonMode.SENTINEL

        then:
        sentinel.addressField == 'sentinelAddresses'
        sentinel.modeRoot == 'sentinelServersConfig'
    }

}

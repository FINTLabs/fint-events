package no.fint.events.queue

import spock.lang.Specification

class QueueNameSpec extends Specification {

    def "Create QueueName with orgId"() {
        when:
        def queueName = QueueName.with('rogfk.no')

        then:
        queueName.env == null
        queueName.component == null
        queueName.orgId == 'rogfk.no'
    }

    def "Create QueueName with component and orgId"() {
        when:
        def queueName = QueueName.with('personal', 'rogfk.no')

        then:
        queueName.env == null
        queueName.component == 'personal'
        queueName.orgId == 'rogfk.no'
    }

    def "Create QueueName with env, component and orgId"() {
        when:
        def queueName = QueueName.with('production', 'personal', 'rogfk.no')

        then:
        queueName.env == 'production'
        queueName.component == 'personal'
        queueName.orgId == 'rogfk.no'
    }
}

package no.fint.events.scheduling

import org.springframework.scheduling.config.IntervalTask
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import spock.lang.Specification

class FintEventsSchedulingSpec extends Specification {

    def "Add fixed delay listener"() {
        given:
        def registrar = Mock(ScheduledTaskRegistrar)
        def scheduling = new FintEventsScheduling()
        scheduling.setRegistrar(registrar)

        when:
        scheduling.register(Mock(Listener))

        then:
        1 * registrar.scheduleFixedDelayTask(_ as IntervalTask)
    }

    def "Add listeners after registrar is set"() {
        given:
        def registrar = Mock(ScheduledTaskRegistrar)
        def scheduling = new FintEventsScheduling()

        when:
        scheduling.register(Mock(Listener))
        scheduling.setRegistrar(registrar)

        then:
        1 * registrar.scheduleFixedDelayTask(_ as IntervalTask)
    }

    def "Do not unregister if listenerId is not found"() {
        given:
        def listenerTasks = [:]
        def scheduling = new FintEventsScheduling(listenerTasks: listenerTasks)

        when:
        scheduling.unregister('123')

        then:
        listenerTasks.size() == 0
    }
}

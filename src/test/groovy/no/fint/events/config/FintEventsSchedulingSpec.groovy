package no.fint.events.config

import no.fint.events.listener.Listener
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
        scheduling.register(new Listener(null, null, null))

        then:
        1 * registrar.scheduleFixedDelayTask(_ as IntervalTask)
    }

    def "Add listeners after registrar is set"() {
        given:
        def registrar = Mock(ScheduledTaskRegistrar)
        def scheduling = new FintEventsScheduling()

        when:
        scheduling.register(new Listener(null, null, null))
        scheduling.setRegistrar(registrar)

        then:
        1 * registrar.scheduleFixedDelayTask(_ as IntervalTask)
    }
}

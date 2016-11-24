package no.fint.events

import no.fint.events.testutils.TestListener
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import spock.lang.Specification

class EventsSpec extends Specification {
    private Events events
    private AmqpAdmin amqpAdmin
    private ConnectionFactory connectionFactory
    private EventsRegistry eventsRegistry

    void setup() {
        amqpAdmin = Mock(AmqpAdmin)
        connectionFactory = Mock(ConnectionFactory)
        eventsRegistry = Mock(EventsRegistry)
        events = new Events(amqpAdmin: amqpAdmin, connectionFactory: connectionFactory, eventsRegistry: eventsRegistry)
    }

    def "Create exchange and queues when registering a message listener"() {
        when:
        def listener = events.registerUnstartedListener(new TopicExchange("my-exchange"), new Queue("my-queue"), TestListener)

        then:
        1 * amqpAdmin.declareExchange(_ as Exchange)
        1 * amqpAdmin.declareQueue(_ as Queue)
        1 * amqpAdmin.declareBinding(_ as Binding)
        1 * eventsRegistry.add(_ as String, _ as Class) >> Optional.of(new SimpleMessageListenerContainer())
        listener.isPresent()
    }

    def "Delete queue and exchange"() {
        when:
        events.deleteQueues(new TopicExchange("my-exchange"), new Queue("my-queue"))

        then:
        1 * amqpAdmin.deleteQueue("my-queue") >> true
        1 * amqpAdmin.deleteExchange("my-exchange") >> true
    }
}

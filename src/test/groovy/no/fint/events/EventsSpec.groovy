package no.fint.events

import no.fint.events.testutils.TestDto
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import spock.lang.Specification

class EventsSpec extends Specification {
    private Events events
    private AmqpAdmin amqpAdmin
    private ConnectionFactory connectionFactory
    private EventsRegistry eventsRegistry

    void setup() {
        amqpAdmin = Mock(AmqpAdmin)
        connectionFactory = Mock(ConnectionFactory) {

        }
        eventsRegistry = Mock(EventsRegistry)
        events = new Events(amqpAdmin: amqpAdmin, connectionFactory: connectionFactory, eventsRegistry: eventsRegistry)
    }

    def "Create exchange and queues when registering an unstarted message listener"() {
        when:
        def listener = events.registerUnstartedListener(new TopicExchange("my-exchange"), new Queue("my-queue"), MessageListener)

        then:
        1 * amqpAdmin.declareExchange(_ as Exchange)
        1 * amqpAdmin.declareQueue(_ as Queue)
        1 * amqpAdmin.declareBinding(_ as Binding)
        1 * eventsRegistry.add(_ as String, _ as Class) >> Optional.of(Mock(SimpleMessageListenerContainer))
        listener.isPresent()
    }

    def "Create exchange and queues when registering a message listener"() {
        when:
        def listener = events.registerListener("my-exchange", "my-queue", MessageListener)

        then:
        1 * amqpAdmin.declareExchange(_ as Exchange)
        1 * amqpAdmin.declareQueue(_ as Queue)
        1 * amqpAdmin.declareBinding(_ as Binding)
        1 * eventsRegistry.add(_ as String, _ as Class) >> Optional.of(Mock(SimpleMessageListenerContainer))
        listener.isPresent()
    }

    def "Delete queue and exchange"() {
        when:
        events.deleteQueues(new TopicExchange("my-exchange"), new Queue("my-queue"))

        then:
        1 * amqpAdmin.deleteQueue("my-queue") >> true
        1 * amqpAdmin.deleteExchange("my-exchange") >> true
    }

    def "Remove registered message listener"() {
        when:
        events.removeListener("my-exchange", "my-queue")

        then:
        1 * amqpAdmin.removeBinding(_ as Binding)
        1 * eventsRegistry.close("my-queue")
    }

    def "Create rabbit template with message converter"() {
        when:
        def rabbitTemplate = events.rabbitTemplate(TestDto)

        then:
        rabbitTemplate.messageConverter instanceof Jackson2JsonMessageConverter
    }

    def "Create rabbit template with connection factory"() {
        when:
        def rabbitTemplate = events.rabbitTemplate()

        then:
        rabbitTemplate.connectionFactory != null
    }
}

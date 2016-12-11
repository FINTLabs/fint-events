package no.fint.events.listeners

import no.fint.events.testutils.TestDto
import no.fint.events.testutils.listeners.ReplyToJsonObjectListener
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import spock.lang.Specification

class EventsReplyToJsonObjectListenerSpec extends Specification {

    def "Invoke method with replyTo queue and json object"() {
        given:
        ReplyToJsonObjectListener replyToJsonObjectListener = new ReplyToJsonObjectListener()
        EventsReplyToJsonObjectListener listener = new EventsReplyToJsonObjectListener(TestDto, replyToJsonObjectListener, 'onReplyToAndObject')
        def message = MessageBuilder.withBody("{\"value\":\"test123\"}".bytes).setContentType(MessageProperties.CONTENT_TYPE_JSON).setReplyTo('reply-to').build()

        when:
        listener.onMessage(message)

        then:
        replyToJsonObjectListener.called
    }
}

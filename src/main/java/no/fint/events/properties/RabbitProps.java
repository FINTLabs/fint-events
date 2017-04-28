package no.fint.events.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RabbitProps {

    @Value("${fint.rabbitmq.host:localhost}")
    private String host;

    @Value("${fint.rabbitmq.port:5672}")
    private int port;

    @Value("${fint.rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Value("${fint.rabbitmq.username:}")
    private String username;

    @Value("${fint.rabbitmq.password:}")
    private String password;

    @Value("${fint.rabbitmq.reply-to-timeout:30000}")
    private int replyToTimeout;

}

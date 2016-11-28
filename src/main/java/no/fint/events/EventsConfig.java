package no.fint.events;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.properties.EventsProps;
import no.fint.events.properties.ListenerProps;
import no.fint.events.properties.RabbitProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class EventsConfig {

    @Autowired
    private RabbitProperties rabbitProperties;

    @PostConstruct
    public void init() {
        String host = rabbitProps().getHost();
        if (isPresent(host)) {
            log.info("Setting rabbitmq host: {}", host);
            rabbitProperties.setHost(host);
        }

        int port = rabbitProps().getPort();
        if (port > 0) {
            log.info("Setting rabbitmq port: {}", port);
            rabbitProperties.setPort(port);
        }

        String username = rabbitProps().getUsername();
        if (isPresent(username)) {
            rabbitProperties.setUsername(username);
        }

        String password = rabbitProps().getPassword();
        if (isPresent(password)) {
            rabbitProperties.setPassword(password);
        }

        String virtualHost = rabbitProps().getVirtualHost();
        if (isPresent(virtualHost)) {
            rabbitProperties.setVirtualHost(virtualHost);
        }
    }

    private boolean isPresent(String value) {
        return !(StringUtils.isEmpty(value));
    }

    @Bean
    public EventsRegistry eventsRegistry() {
        return new EventsRegistry();
    }

    @Bean
    public Events eventsQueues() {
        return new Events();
    }

    @Bean
    public FintEvents fintEvents() {
        return new FintEvents();
    }

    @Bean
    public EventsProps eventsProps() {
        return new EventsProps();
    }

    @Bean
    public RabbitProps rabbitProps() {
        return new RabbitProps();
    }

    @Bean
    public ListenerProps listenerProps() {
        return new ListenerProps();
    }
}

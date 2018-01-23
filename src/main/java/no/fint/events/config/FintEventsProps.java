package no.fint.events.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class FintEventsProps {
    static final String FINT_HAZELCAST_MEMBERS = "fint.hazelcast.members";

    @Value("${fint.hazelcast.config:fint-hazelcast.xml}")
    private String hazelcastConfig;

    @Value("${" + FINT_HAZELCAST_MEMBERS + ":}")
    private String[] hazelcastMembers;

    @PostConstruct
    public void init() {
        System.out.println("");
    }

}

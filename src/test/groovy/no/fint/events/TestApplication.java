package no.fint.events;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import no.fint.events.annotations.EnableFintEvents;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableFintEvents
@SpringBootApplication
public class TestApplication {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance();
    }
}

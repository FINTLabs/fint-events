package no.fint.events.testutils;

import no.fint.events.annotations.EnableFintEvents;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableFintEvents
@SpringBootApplication
public class TestApplication {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

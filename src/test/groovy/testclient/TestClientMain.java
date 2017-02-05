package testclient;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.EnableFintEvents;
import no.fint.events.FintEvents;
import no.fint.events.testutils.TestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * Run with testclient profile
 */
@Slf4j
@EnableScheduling
@EnableFintEvents
@SpringBootApplication
public class TestClientMain {
    private static final String ORGID = "rogfk.no";

    @Autowired
    private FintEvents fintEvents;

    @PostConstruct
    public void init() {
        fintEvents.setDefaultType(TestDto.class);
        fintEvents.registerDownstreamListener(ORGID, TestDownstreamSubscriber.class);
    }

    @Scheduled(initialDelay = 10000L, fixedRate = 5000L)
    public void sendReplyToMessage() {
        TestDto testDto = new TestDto();
        testDto.setValue("testing");
        Optional<TestDto> response = fintEvents.sendAndReceiveDownstream(ORGID, testDto, TestDto.class);
        if (response.isPresent()) {
            log.info("Reply-to response: {}", response.get());
        } else {
            log.info("No response");
        }
    }

    @Scheduled(initialDelay = 15000L, fixedRate = 7000L)
    public void sendMessage() {
        TestDto testDto = new TestDto();
        testDto.setValue("testing");
        fintEvents.sendDownstream(ORGID, testDto);
        log.info("Message sent");
    }

    public static void main(String[] args) {
        SpringApplication.run(TestClientMain.class, args);
    }
}

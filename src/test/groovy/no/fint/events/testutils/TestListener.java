package no.fint.events.testutils;

import lombok.Getter;
import no.fint.events.annotations.FintEventListener;
import no.fint.events.queue.QueueType;
import org.springframework.stereotype.Component;

@Component
public class TestListener {
    @Getter
    private TestDto testDto;

    @FintEventListener(type = QueueType.DOWNSTREAM)
    public void receive(TestDto testDto) {
        this.testDto = testDto;
    }

    public void test(TestDto testDto) {
    }
}

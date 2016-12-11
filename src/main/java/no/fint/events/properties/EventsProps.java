package no.fint.events.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class EventsProps {

    @Value("${fint.events.orgs:}")
    private String[] organisations;

    @Value("${fint.events.default-downstream-queue:%s.downstream}")
    private String defaultDownstreamQueue;

    @Value("${fint.events.default-upstream-queue:%s.upstream}")
    private String defaultUpstreamQueue;

    @Value("${fint.events.default-undelivered-queue:%s.undelivered}")
    private String defaultUndeliveredQueue;

    @Value("${fint.events.test-mode:false}")
    private String testMode;

    public boolean isTestMode() {
        return Boolean.valueOf(testMode);
    }
}

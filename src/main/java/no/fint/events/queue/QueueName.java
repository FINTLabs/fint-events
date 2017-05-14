package no.fint.events.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueName {
    private String env;
    private String component;
    private String orgId;

    public static QueueName with(String orgId) {
        return new QueueName(null, null, orgId);
    }

    public static QueueName with(String component, String orgId) {
        return new QueueName(null, component, orgId);
    }

    public static QueueName with(String env, String component, String orgId) {
        return new QueueName(env, component, orgId);
    }
}

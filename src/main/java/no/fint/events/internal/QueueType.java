package no.fint.events.internal;

import lombok.Getter;

public enum QueueType {
    UPSTREAM("no.fint.upstream"),
    DOWNSTREAM("no.fint.downstream");

    @Getter
    private String queueName;

    QueueType(String queueName) {
        this.queueName = queueName;
    }
}

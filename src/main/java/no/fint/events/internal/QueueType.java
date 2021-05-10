package no.fint.events.internal;

public enum QueueType {
    UPSTREAM("no.fint.upstream"),
    DOWNSTREAM("no.fint.downstream");

    private String queueName;

    public String getQueueName(String orgId) {
        return queueName + "." + orgId;
    }

    QueueType(String queueName) {
        this.queueName = queueName;
    }
}

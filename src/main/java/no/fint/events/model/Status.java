package no.fint.events.model;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum Status {
    NEW,
    SENT_TO_INBOUND_QUEUED,
    INBOUND_QUEUED,
    DELIVERED_TO_PROVIDER,
    PROVIDER_CONFIRMED,
    PROVIDER_REJECTED,
    ORPHANT,
    PROVIDER_RESPONSE,
    SENT_TO_OUTBOUND_QUEUE,
    OUTBOUND_QUEUE,
    SENT_TO_CLIENT
}
package no.fint.events.config;

import lombok.Getter;

public enum RedissonMode {
    CLUSTER("nodeAddresses"),
    REPLICATED("nodeAddresses"),
    SINGLE("address", false),
    SENTINEL("sentinelAddresses");

    private boolean plural;

    @Getter
    private String addressField;

    RedissonMode(String addressField) {
        this.addressField = addressField;
        plural = true;
    }

    RedissonMode(String addressField, boolean plural) {
        this.addressField = addressField;
        this.plural = plural;
    }

    public String getModeRoot() {
        if (plural) {
            return String.format("%sServersConfig", this.name().toLowerCase());
        } else {
            return String.format("%sServerConfig", this.name().toLowerCase());
        }
    }
}

package no.fint.events.config;

public enum RedisConfiguration {
    ;

    public static final String SINGLE = "single";
    public static final String MASTER_SLAVE = "master-slave";
    public static final String SENTINEL = "sentinel";
    public static final String CLUSTERED = "clustered";
    public static final String REPLICATED = "replicated";

    public static boolean isSingle(String value) {
        return SINGLE.equals(value);
    }

    public static boolean isMasterSlave(String value) {
        return MASTER_SLAVE.equals(value);
    }

    public static boolean isSentinel(String value) {
        return SENTINEL.equals(value);
    }

    public static boolean isClustered(String value) {
        return CLUSTERED.equals(value);
    }

    public static boolean isReplicated(String value) {
        return REPLICATED.equals(value);
    }
}

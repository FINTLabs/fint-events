package no.fint.events.config;

public enum RedisConfiguration {
    ;

    public static final String SINGLE = "single";

    public static boolean isSingle(String value) {
        return SINGLE.equals(value);
    }

}

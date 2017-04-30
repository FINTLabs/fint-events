package no.fint.events;

public interface HealthCheck<T> {
    T check(T event);
}

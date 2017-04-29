package no.fint.events;

public interface Health<T> {
    T healthCheck(T event);
}

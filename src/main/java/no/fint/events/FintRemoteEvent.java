package no.fint.events;

public interface FintRemoteEvent<T> {

    T request(T value);

}

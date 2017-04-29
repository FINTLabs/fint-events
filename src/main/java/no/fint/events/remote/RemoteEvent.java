package no.fint.events.remote;

public interface RemoteEvent<T> {

    T request(T value);

}

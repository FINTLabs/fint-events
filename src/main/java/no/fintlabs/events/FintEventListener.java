package no.fintlabs.events;

import no.fint.event.model.Event;

import java.util.function.Consumer;

public interface FintEventListener extends Consumer<Event> {
}

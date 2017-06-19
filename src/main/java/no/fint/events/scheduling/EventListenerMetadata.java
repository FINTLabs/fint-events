package no.fint.events.scheduling;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
class EventListenerMetadata {
    private Class clazz;
    private Method method;
}

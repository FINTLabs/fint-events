package no.fint.events.listener;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
class EventListenerMetadata {
    private Class clazz;
    private Method method;
}

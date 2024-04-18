package no.fintlabs.events.annotations;

import no.fintlabs.events.config.FintEventsConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(FintEventsConfig.class)
public @interface EnableFintEvents {
}

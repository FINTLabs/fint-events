package no.fint.events.scheduling;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.client.RedisException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;

@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Listener implements Runnable {
    @Getter
    private String id;
    @Getter
    private String registered;
    @Getter
    @JsonIgnore
    private Object object;
    private Method method;
    @Getter
    private String queueName;
    private BlockingQueue queue;

    public Listener(Object object, Method method, String queueName, BlockingQueue queue) {
        this.id = String.format("%s-%s-%s", object.getClass().getName(), method.getName(), queueName);

        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        this.registered = dateTime.format(DateTimeFormatter.ISO_INSTANT);

        this.object = object;
        this.method = method;
        this.queue = queue;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        try {
            Object response = queue.take();
            if (response != null) {
                method.invoke(object, response);
            }
        } catch (InterruptedException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            log.error("Unable to call listener, bean:{} method:{}. Exception message:{}, cause:{}, type:{}", object.getClass().getName(), method.getName(), e.getMessage(), getCauseString(e), e.getClass().getName());
        } catch (RedisException | CompletionException e) {
            if (e instanceof RedissonShutdownException || e.getCause() instanceof RedissonShutdownException) {
                log.debug("Listener task stopped because redisson is shutting down. {}", e.getMessage());
            } else {
                log.error("Exception when trying to read message from redisson queue, {}", e.getMessage());
            }
        }
    }

    private String getCauseString(Exception e) {
        if (e.getCause() == null) {
            return "";
        } else {
            return e.getCause().getMessage();
        }
    }

    @JsonGetter("object")
    public String getObjectAsString() {
        return (object == null ? null : object.getClass().getName());
    }

    @JsonGetter("method")
    public String getMethodAsString() {
        return (method == null ? null : method.getName());
    }
}

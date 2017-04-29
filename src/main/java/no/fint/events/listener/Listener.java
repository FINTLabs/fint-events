package no.fint.events.listener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Data
@AllArgsConstructor
public class Listener implements Runnable {
    private Object object;
    private Method method;
    private BlockingQueue queue;

    @Override
    public void run() {
        try {
            Object response = queue.poll();
            if (response != null) {
                method.invoke(object, response);
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            log.error("Unable to call listener, bean:{} method:{}. Exception: {}", object.getClass().getName(), method.getName(), e.getMessage());
        } catch (RedisException e) {
            log.warn("Exception when reading from the queue, is the system shutting down? {}", e.getMessage());
        }
    }
}

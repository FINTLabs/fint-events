package no.fint.events.internal;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

@Slf4j
@Component
public class FintEventsHealth implements MessageListener<Event> {
    private final Map<String, BlockingQueue<Event>> waiters = new HashMap<>();

    public BlockingQueue<Event> register(String id) {
        BlockingQueue<Event> queue = new SynchronousQueue<>();
        waiters.put(id, queue);
        return queue;
    }

    @Override
    public void onMessage(Message<Event> message) {
        Event event = message.getMessageObject();
        if (event.isHealthCheck()) {
            BlockingQueue<Event> queue = waiters.remove(event.getCorrId());
            if(queue == null) {
                log.debug("No queue found for event: {}", event);
            } else {
                queue.offer(event);
            }
        }
    }
}

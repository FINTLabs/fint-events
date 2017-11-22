package no.fint.events.internal;

import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

@Slf4j
@Component
public class FintEventsHealth implements ItemListener<Event> {
    private final Map<String, BlockingQueue<Event>> waiters = new HashMap<>();

    public BlockingQueue<Event> register(String id) {
        BlockingQueue<Event> queue = new SynchronousQueue<>();
        waiters.put(id, queue);
        return queue;
    }

    @Override
    public void itemAdded(ItemEvent<Event> item) {
        Event event = item.getItem();
        if (event.isHealthCheck()) {
            BlockingQueue<Event> queue = waiters.remove(event.getCorrId());
            if(queue == null) {
                log.warn("No queue found for event: {}", event);
            } else {
                queue.offer(event);
            }
        }
    }

    @Override
    public void itemRemoved(ItemEvent<Event> item) {
    }

}

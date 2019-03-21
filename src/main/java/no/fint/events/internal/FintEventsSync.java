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
public class FintEventsSync extends FintEventsAbstract {

    @Override
    public void itemAdded(ItemEvent<Event> item) {
        offer(item.getItem());
    }

    @Override
    public void itemRemoved(ItemEvent<Event> item) {
    }

}

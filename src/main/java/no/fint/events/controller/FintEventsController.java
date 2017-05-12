package no.fint.events.controller;

import com.google.common.collect.ImmutableMap;
import no.fint.events.FintEvents;
import no.fint.events.config.FintEventsProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@ConditionalOnProperty(value = FintEventsProps.QUEUE_ENDPOINT_ENABLED, havingValue = "true")
@RestController
@RequestMapping(value = "/fint-events/queues", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FintEventsController {

    @Autowired
    private FintEvents fintEvents;

    @GetMapping
    public Set<String> getQueues() {
        return fintEvents.getQueues();
    }

    @GetMapping("/{queue:.+}")
    public ResponseEntity getQueueContent(@PathVariable String queue, @RequestParam(required = false) Integer index) {
        BlockingQueue<Object> q = fintEvents.getQueue(queue);
        int size = q.size();
        Object value = getValue(q, index);
        return ResponseEntity.ok(ImmutableMap.of(
                "size", String.valueOf(size),
                "value", getStringValue(value)
        ));
    }

    private Object getValue(BlockingQueue queue, Integer index) {
        if (index == null) {
            return queue.peek();
        } else {
            Object[] values = queue.toArray();
            if (values.length >= (index + 1)) {
                return Arrays.asList(values).get(index);
            }
            return "";
        }
    }

    private String getStringValue(Object nextValue) {
        String value = (nextValue == null) ? "" : nextValue.toString();
        value = value.substring(0, Math.min(value.length(), 300));
        return value;
    }

}

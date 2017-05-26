package no.fint.events.controller;

import com.google.common.collect.ImmutableMap;
import no.fint.events.FintEvents;
import no.fint.events.config.FintEventsProps;
import no.fint.events.config.RedissonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

@ConditionalOnProperty(value = FintEventsProps.QUEUE_ENDPOINT_ENABLED, havingValue = "true")
@RestController
@RequestMapping(value = "/fint-events", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FintEventsController {

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private RedissonConfig redissonConfig;

    @GetMapping("/queues")
    public Map<String, Set<String>> getQueues() {
        return ImmutableMap.of(
                "componentQueues", fintEvents.getComponentQueues(),
                "queues", fintEvents.getQueues()
        );
    }

    @GetMapping("/queues/{queue:.+}")
    public ResponseEntity getQueueContent(@PathVariable String queue, @RequestParam(required = false) Integer index) {
        BlockingQueue<Object> q = fintEvents.getQueue(queue);
        int size = q.size();
        Object value = getValue(q, index);
        return ResponseEntity.ok(ImmutableMap.of(
                "size", String.valueOf(size),
                "value", getStringValue(value)
        ));
    }

    @GetMapping("/listeners")
    public Map<String, Class> getListeners() {
        return fintEvents.getListeners();
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

    @GetMapping("/redissonConfig")
    public String getRedissonConfig() {
        return redissonConfig.getRedissonJsonConfig();
    }

}

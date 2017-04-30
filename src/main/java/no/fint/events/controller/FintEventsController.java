package no.fint.events.controller;

import com.google.common.collect.ImmutableMap;
import no.fint.events.FintEvents;
import no.fint.events.config.FintEventsProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.BlockingQueue;

@RestController
@RequestMapping(value = "/fint-events", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FintEventsController {

    @Autowired
    private FintEventsProps props;

    @Autowired
    private FintEvents fintEvents;

    @GetMapping("/{queue}")
    public ResponseEntity getQueueContent(@PathVariable String queue) {
        if (Boolean.valueOf(props.getQueueEndpointEnabled())) {
            BlockingQueue<Object> q = fintEvents.getQueue(queue);
            int size = q.size();
            Object nextValue = q.peek();
            return ResponseEntity.ok(ImmutableMap.of(
                    "size", String.valueOf(size),
                    "nextValue", (nextValue == null) ? "" : nextValue.toString()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}

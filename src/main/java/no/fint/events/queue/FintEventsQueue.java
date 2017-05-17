package no.fint.events.queue;

import com.google.common.collect.ImmutableMap;
import no.fint.events.config.FintEventsProps;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class FintEventsQueue {

    @Autowired
    private FintEventsProps props;

    public String getDownstreamQueueName(QueueName queueName) {
        Map<String, String> valueMap = createValueMap(queueName.getComponent(), queueName.getEnv(), queueName.getOrgId());
        return StrSubstitutor.replace(props.getDefaultDownstreamQueue(), valueMap, "{", "}");
    }

    public String getUpstreamQueueName(QueueName queueName) {
        Map<String, String> valueMap = createValueMap(queueName.getComponent(), queueName.getEnv(), queueName.getOrgId());
        return StrSubstitutor.replace(props.getDefaultUpstreamQueue(), valueMap, "{", "}");
    }

    private Map<String, String> createValueMap(String component, String env, String orgId) {
        return ImmutableMap.of(
                "component", (StringUtils.isEmpty(component)) ? props.getComponent() : component,
                "env", (StringUtils.isEmpty(env)) ? props.getEnv() : env,
                "orgId", orgId
        );
    }

}

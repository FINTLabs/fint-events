package no.fint.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.util.MethodInvoker;

import java.io.IOException;

@Slf4j
public class EventsJsonObjectListener extends MessageListenerAdapter {

    private final ObjectMapper objectMapper;
    private final Class<?> responseObject;

    public EventsJsonObjectListener(ObjectMapper objectMapper, Class<?> responseObject, Object target, String method) {
        super(target, method);
        this.objectMapper = objectMapper;
        this.responseObject = responseObject;
    }

    @Override
    protected Object invokeListenerMethod(String methodName, Object[] arguments, Message originalMessage) throws Exception {
        try {
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(this.getDelegate());
            methodInvoker.setTargetMethod(methodName);

            byte[] body = originalMessage.getBody();
            if (body == null) {
                throw new IllegalArgumentException("The message body is null");
            }

            String json = new String(body);
            Object response = objectMapper.readValue(json, responseObject);
            methodInvoker.setArguments(new Object[]{response});

            methodInvoker.prepare();
            return methodInvoker.invoke();
        } catch (IllegalArgumentException | IOException e) {
            log.error("Unable to process message body as json");
            throw new IllegalArgumentException("Unable to process message body as json", e);
        } catch (Exception ex) {
            throw new ListenerExecutionFailedException("Failed to invoke target method " + methodName, ex);
        }
    }
}

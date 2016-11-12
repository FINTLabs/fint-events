package no.fint.events.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.util.MethodInvoker;

import java.util.Map;

public class EventsHeaderAndBodyListener extends MessageListenerAdapter {

    public EventsHeaderAndBodyListener(Object target, String method) {
        super(target, method);
    }

    @Override
    protected Object invokeListenerMethod(String methodName, Object[] arguments, Message originalMessage) throws Exception {
        try {
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(this.getDelegate());
            methodInvoker.setTargetMethod(methodName);

            Map<String, Object> headers = originalMessage.getMessageProperties().getHeaders();
            byte[] body = originalMessage.getBody();
            methodInvoker.setArguments(new Object[]{headers, body});
            
            methodInvoker.prepare();
            return methodInvoker.invoke();
        } catch (Exception ex) {
            throw new ListenerExecutionFailedException("Failed to invoke target method " + methodName, ex);
        }

    }
}

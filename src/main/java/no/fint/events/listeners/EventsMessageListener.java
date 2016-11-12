package no.fint.events.listeners;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException;
import org.springframework.util.MethodInvoker;

public class EventsMessageListener extends MessageListenerAdapter {

    public EventsMessageListener(Object target, String method) {
        super(target, method);
    }

    @Override
    protected Object invokeListenerMethod(String methodName, Object[] arguments, Message originalMessage) throws Exception {
        try {
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(this.getDelegate());
            methodInvoker.setTargetMethod(methodName);
            methodInvoker.setArguments(new Object[]{originalMessage});
            methodInvoker.prepare();
            return methodInvoker.invoke();
        } catch (Exception ex) {
            throw new ListenerExecutionFailedException("Failed to invoke target method " + methodName, ex);
        }
    }
}

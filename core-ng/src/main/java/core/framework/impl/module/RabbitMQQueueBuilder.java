package core.framework.impl.module;

import core.framework.api.queue.MessagePublisher;
import core.framework.impl.queue.MessageValidator;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQEndpoint;
import core.framework.impl.queue.RabbitMQListener;
import core.framework.impl.queue.RabbitMQPublisher;

/**
 * @author neo
 */
// load rabbitmq class when needed
public class RabbitMQQueueBuilder {
    private final ModuleContext context;

    public RabbitMQQueueBuilder(ModuleContext context) {
        this.context = context;
    }

    public RabbitMQListener listener(String queue) {
        RabbitMQListener listener = new RabbitMQListener(rabbitMQ(), queue, context.executor, context.queueManager.validator());
        if (!context.test) {
            context.startupHook.add(listener::start);
            context.shutdownHook.add(listener::shutdown);
        }
        return listener;
    }

    public <T> MessagePublisher<T> publisher(RabbitMQEndpoint endpoint, Class<T> messageClass, MessageValidator validator) {
        return new RabbitMQPublisher<>(rabbitMQ(), endpoint, messageClass, validator);
    }

    private RabbitMQ rabbitMQ() {
        if (context.queueManager.rabbitMQ == null) {
            throw new Error("rabbitMQ is not configured, please use queue().rabbitMQ() to configure");
        }
        return context.queueManager.rabbitMQ;
    }
}

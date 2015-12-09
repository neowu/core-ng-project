package core.framework.api.module;

import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.queue.MessageValidator;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQEndpoint;
import core.framework.impl.queue.RabbitMQListener;
import core.framework.impl.queue.RabbitMQPublisher;
import core.framework.impl.resource.RefreshPoolJob;
import core.framework.impl.scheduler.FixedRateTrigger;

import java.time.Duration;

/**
 * @author neo
 */
public final class QueueConfig {
    private final ModuleContext context;

    public QueueConfig(ModuleContext context) {
        this.context = context;
    }

    public RabbitMQConfig rabbitMQ() {
        if (context.queueManager.rabbitMQ == null) {
            context.queueManager.rabbitMQ = new RabbitMQ();
            if (!context.isTest()) {
                context.scheduler().addTrigger(new FixedRateTrigger("refresh-rabbitmq-pool", new RefreshPoolJob(context.queueManager.rabbitMQ.pool), Duration.ofMinutes(5)));
                context.shutdownHook.add(context.queueManager.rabbitMQ::close);
            }
        }
        return new RabbitMQConfig(context);
    }

    public MessageHandlerConfig subscribe(String queueURI) {
        return context.queueManager.listeners().computeIfAbsent(queueURI, this::listener);
    }

    public <T> void publish(String destination, Class<T> messageClass) {
        MessageValidator validator = context.queueManager.validator();
        validator.register(messageClass);
        MessagePublisher<T> publisher = publisher(destination, messageClass);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), null, publisher);
    }

    private MessageHandlerConfig listener(String queueURI) {
        RabbitMQListener listener = new RabbitMQListener(context.queueManager.rabbitMQ(), new RabbitMQEndpoint(queueURI).routingKey, context.executor, context.queueManager.validator(), context.logManager);
        if (!context.isTest()) {
            context.startupHook.add(listener::start);
            context.shutdownHook.add(listener::stop);
        }
        return listener;
    }

    @SuppressWarnings("unchecked")
    private <T> MessagePublisher<T> publisher(String uri, Class<T> messageClass) {
        if (context.isTest()) {
            return context.mockFactory.create(MessagePublisher.class, uri, context.queueManager.validator());
        } else {
            return new RabbitMQPublisher<>(context.queueManager.rabbitMQ(), new RabbitMQEndpoint(uri), messageClass, context.queueManager.validator(), context.logManager);
        }
    }
}

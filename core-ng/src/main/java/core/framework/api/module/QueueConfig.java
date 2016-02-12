package core.framework.api.module;

import core.framework.api.queue.MessagePublisher;
import core.framework.api.util.Types;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.queue.MessageValidator;
import core.framework.impl.queue.RabbitMQ;
import core.framework.impl.queue.RabbitMQImpl;
import core.framework.impl.queue.RabbitMQListener;
import core.framework.impl.queue.RabbitMQPublisher;

import java.time.Duration;

/**
 * @author neo
 */
public final class QueueConfig {
    private final ModuleContext context;
    private final RabbitMQ rabbitMQ;

    public QueueConfig(ModuleContext context) {
        this.context = context;
        if (context.beanFactory.registered(RabbitMQ.class, null)) {
            rabbitMQ = context.beanFactory.bean(RabbitMQ.class, null);
        } else {
            if (context.isTest()) {
                rabbitMQ = context.mockFactory.create(RabbitMQ.class);
            } else {
                RabbitMQImpl rabbitMQ = new RabbitMQImpl();
                context.backgroundTask().scheduleWithFixedDelay(rabbitMQ.pool::refresh, Duration.ofMinutes(5));
                context.shutdownHook.add(rabbitMQ::close);
                this.rabbitMQ = rabbitMQ;
            }
            context.beanFactory.bind(RabbitMQ.class, null, rabbitMQ);
        }
    }

    public MessageHandlerConfig subscribe(String queue) {
        return context.queueManager.listeners().computeIfAbsent(queue, key -> {
            RabbitMQListener listener = new RabbitMQListener(rabbitMQ, queue, context.executor, context.queueManager.validator(), context.logManager);
            if (!context.isTest()) {
                context.startupHook.add(listener::start);
                context.shutdownHook.add(listener::stop);
            }
            return listener;
        });
    }

    public <T> void publish(String exchange, String routingKey, Class<T> messageClass) {
        MessageValidator validator = context.queueManager.validator();
        validator.register(messageClass);
        MessagePublisher<T> publisher = new RabbitMQPublisher<>(rabbitMQ, exchange, routingKey, messageClass, context.queueManager.validator(), context.logManager);
        context.beanFactory.bind(Types.generic(MessagePublisher.class, messageClass), null, publisher);
    }

    public void hosts(String... hosts) {
        if (!context.isTest()) {
            ((RabbitMQImpl) rabbitMQ).hosts(hosts);
        }
    }

    public void user(String user) {
        if (!context.isTest()) {
            ((RabbitMQImpl) rabbitMQ).user(user);
        }
    }

    public void password(String password) {
        if (!context.isTest()) {
            ((RabbitMQImpl) rabbitMQ).password(password);
        }
    }

    public void timeout(Duration timeout) {
        if (!context.isTest()) {
            ((RabbitMQImpl) rabbitMQ).timeout(timeout);
        }
    }

    public void poolSize(int minSize, int maxSize) {
        if (!context.isTest()) {
            ((RabbitMQImpl) rabbitMQ).pool.size(minSize, maxSize);
        }
    }
}

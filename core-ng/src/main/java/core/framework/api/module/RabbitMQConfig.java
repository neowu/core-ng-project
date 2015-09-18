package core.framework.api.module;

import core.framework.impl.module.ModuleContext;

import java.time.Duration;

/**
 * @author neo
 */
public final class RabbitMQConfig {
    private final ModuleContext context;

    public RabbitMQConfig(ModuleContext context) {
        this.context = context;
    }

    public void hosts(String... hosts) {
        if (!context.test) {
            context.queueManager.rabbitMQ.hosts(hosts);
        }
    }

    public void user(String user) {
        if (!context.test) {
            context.queueManager.rabbitMQ.user(user);
        }
    }

    public void password(String password) {
        if (!context.test) {
            context.queueManager.rabbitMQ.password(password);
        }
    }

    public void timeout(Duration timeout) {
        context.queueManager.rabbitMQ.timeout(timeout);
    }

    public void poolSize(int minSize, int maxSize) {
        context.queueManager.rabbitMQ.pool.size(minSize, maxSize);
    }
}

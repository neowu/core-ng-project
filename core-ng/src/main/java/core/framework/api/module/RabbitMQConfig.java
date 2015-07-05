package core.framework.api.module;

import core.framework.impl.module.ModuleContext;

import java.time.Duration;

/**
 * @author neo
 */
public class RabbitMQConfig {
    private final ModuleContext context;

    public RabbitMQConfig(ModuleContext context) {
        this.context = context;
    }

    public RabbitMQConfig hosts(String... hosts) {
        if (!context.test) {
            context.queueManager.rabbitMQ.hosts(hosts);
        }
        return this;
    }

    public RabbitMQConfig user(String user) {
        if (!context.test) {
            context.queueManager.rabbitMQ.connectionFactory.setUsername(user);
        }
        return this;
    }

    public RabbitMQConfig password(String password) {
        if (!context.test) {
            context.queueManager.rabbitMQ.connectionFactory.setPassword(password);
        }
        return this;
    }

    public RabbitMQConfig connectionTimeout(Duration timeout) {
        context.queueManager.rabbitMQ.connectionTimeout(timeout);
        return this;
    }
}

package core.framework.api.module;

import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * @author neo
 */
public final class RabbitMQConfig {
    private final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    private final ModuleContext context;

    public RabbitMQConfig(ModuleContext context) {
        this.context = context;
    }

    public RabbitMQConfig hosts(String... hosts) {
        if (!context.test) {
            logger.info("set rabbitMQ hosts, hosts={}", Arrays.toString(hosts));
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

    public RabbitMQConfig timeout(Duration timeout) {
        context.queueManager.rabbitMQ.timeout(timeout);
        return this;
    }
}

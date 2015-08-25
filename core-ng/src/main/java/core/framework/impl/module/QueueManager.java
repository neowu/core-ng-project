package core.framework.impl.module;

import core.framework.api.module.MessageHandlerConfig;
import core.framework.api.util.Maps;
import core.framework.impl.queue.MessageValidator;
import core.framework.impl.queue.RabbitMQ;

import java.util.Map;

/**
 * @author neo
 */
public class QueueManager {
    public RabbitMQ rabbitMQ;
    private MessageValidator validator;
    private Map<String, MessageHandlerConfig> listeners = Maps.newHashMap();

    public Map<String, MessageHandlerConfig> listeners() {
        if (listeners == null) {
            listeners = Maps.newHashMap();
        }
        return listeners;
    }

    public MessageValidator validator() {
        if (validator == null) {
            validator = new MessageValidator();
        }
        return validator;
    }

    public RabbitMQ rabbitMQ() {
        if (rabbitMQ == null) {
            throw new Error("rabbitMQ is not configured, please use queue().rabbitMQ() to configure");
        }
        return rabbitMQ;
    }
}

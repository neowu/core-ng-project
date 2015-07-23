package core.framework.impl.module;

import core.framework.impl.queue.MessageValidator;
import core.framework.impl.queue.RabbitMQ;

/**
 * @author neo
 */
public class QueueManager {
    public RabbitMQ rabbitMQ;
    private MessageValidator validator;

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

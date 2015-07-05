package core.framework.impl.queue;

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
}

package core.log.queue;

/**
 * @author neo
 */
public class RabbitMQMessage<T> {
    public final long deliveryTag;
    public final T body;
    public final int messageSize;

    public RabbitMQMessage(long deliveryTag, T body, int messageSize) {
        this.deliveryTag = deliveryTag;
        this.body = body;
        this.messageSize = messageSize;
    }
}

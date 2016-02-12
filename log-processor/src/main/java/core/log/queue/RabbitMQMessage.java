package core.log.queue;

/**
 * @author neo
 */
public class RabbitMQMessage {
    public final long deliveryTag;
    public final byte[] body;

    public RabbitMQMessage(long deliveryTag, byte[] body) {
        this.deliveryTag = deliveryTag;
        this.body = body;
    }
}

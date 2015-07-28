package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    void publish(T message);

    // RabbitMQ uses routingKey as queueName with default exchange, SNS/SQS don't support routingKey
    void publish(String routingKey, T message);
}

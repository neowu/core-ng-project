package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    void publish(T message);

    // with rabbitmq, the queue is queueName, with SQS, it is full sqs url
    void reply(String queue, T message);
}

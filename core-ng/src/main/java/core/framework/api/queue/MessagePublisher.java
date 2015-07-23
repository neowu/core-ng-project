package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    void publish(T message);

    // with rabbitMQ, the queue is queueName, with SQS, it is full sqs url
    void publish(String queue, T message);
}

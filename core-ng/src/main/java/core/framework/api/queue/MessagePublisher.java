package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    default void publish(T message) {
        publish(message, 0);
    }

    void publish(T message, int priority);  // priority range is determined by "x-max-priority" argument when creating the queue

    void publish(String exchange, String routingKey, T message);
}

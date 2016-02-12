package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    void publish(T message);

    void publish(String exchange, String routingKey, T message);
}

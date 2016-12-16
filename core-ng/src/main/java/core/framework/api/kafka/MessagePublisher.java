package core.framework.api.kafka;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    void publish(String key, T value);

    void publish(String topic, String key, T value);
}

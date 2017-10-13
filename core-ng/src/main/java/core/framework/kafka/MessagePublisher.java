package core.framework.kafka;

import java.util.UUID;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    default void publish(T value) {
        publish(UUID.randomUUID().toString(), value);
    }

    void publish(String key, T value);

    void publish(String topic, String key, T value);
}

package core.framework.kafka;

import javax.annotation.Nullable;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    // kafka uses sticky partitioning if key is null, refer to org.apache.kafka.clients.producer.internals.DefaultPartitioner
    default void publish(T value) {
        publish(null, value);
    }

    void publish(@Nullable String key, T value);
}

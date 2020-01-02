package core.framework.kafka;

/**
 * @author neo
 */
public interface MessagePublisher<T> {
    // kafka uses sticky partitioning if key is null, refer to org.apache.kafka.clients.producer.internals.DefaultPartitioner
    default void publish(T value) {
        publish(null, value);
    }

    void publish(String key, T value);

    void publish(String topic, String key, T value);
}

package core.framework.api.kafka;

/**
 * @author neo
 */
public interface MessageProducer<T> {
    void send(String key, T message);

    void send(String topic, String key, T message);
}

package core.framework.kafka;

/**
 * @author neo
 */
public final class Message<T> {
    public final String key;
    public final T value;

    public Message(String key, T value) {
        this.key = key;
        this.value = value;
    }
}

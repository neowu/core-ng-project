package core.framework.api.kafka;

/**
 * @author neo
 */
public class Message<T> {
    public String key;
    public T value;
}

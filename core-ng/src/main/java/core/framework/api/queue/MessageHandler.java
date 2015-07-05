package core.framework.api.queue;

/**
 * @author neo
 */
public interface MessageHandler<T> {
    void handle(T message) throws Exception;
}

package core.framework.api.queue;

/**
 * @author neo
 */
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(T message) throws Exception;
}

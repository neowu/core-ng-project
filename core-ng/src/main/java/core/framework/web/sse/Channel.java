package core.framework.web.sse;

public interface Channel<T> {
    void send(String id, T event);

    default void send(T event) {
        send(null, event);
    }

    void close();

    void join(String group);

    void leave(String group);
}

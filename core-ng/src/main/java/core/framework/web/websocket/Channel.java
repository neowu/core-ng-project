package core.framework.web.websocket;

/**
 * @author neo
 */
public interface Channel {
    void send(String message);

    Context context();

    void close();

    void join(String room);

    void leave(String room);

    interface Context {
        Object get(String key); // channel is stateful, context key usually be static put onConnect, so in most of case get(key) expect result, that's why here is designed to return Object, not Optional<T>

        void put(String key, Object value);
    }
}

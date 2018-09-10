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
        Object get(String key);

        void put(String key, Object value);
    }
}

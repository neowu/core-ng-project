package core.framework.web.websocket;

import java.util.Optional;

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
        Optional<Object> get(String key);

        void put(String key, Object value);
    }
}

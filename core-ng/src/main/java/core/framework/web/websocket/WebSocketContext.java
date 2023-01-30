package core.framework.web.websocket;

import java.util.List;

/**
 * @author neo
 */
public interface WebSocketContext {
    <V> List<Channel<V>> all();

    <V> List<Channel<V>> room(String name);
}

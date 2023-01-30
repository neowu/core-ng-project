package core.framework.web.websocket;

import java.util.List;
import java.util.Optional;

/**
 * @author neo
 */
public interface WebSocketContext {
    <V> List<Channel<V>> all();

    <V> List<Channel<V>> room(String name);

    <V> Optional<Channel<V>> key(String key);
}

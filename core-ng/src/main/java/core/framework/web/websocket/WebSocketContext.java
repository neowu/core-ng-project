package core.framework.web.websocket;

import java.util.List;

/**
 * @author neo
 */
public interface WebSocketContext {
    List<Channel> all();

    List<Channel> room(String name);
}

package core.framework.web.websocket;

import java.util.Set;

/**
 * @author neo
 */
public interface WebSocketContext {
    Set<Channel> room(String name);
}

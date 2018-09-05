package core.framework.web.websocket;

import java.util.Map;

/**
 * @author neo
 */
public interface Channel {
    void send(String message);

    Map<String, Object> context();

    void close();
}

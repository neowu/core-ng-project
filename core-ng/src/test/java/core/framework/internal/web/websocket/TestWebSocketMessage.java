package core.framework.internal.web.websocket;

import core.framework.api.json.Property;
import core.framework.api.validate.NotNull;

/**
 * @author neo
 */
public class TestWebSocketMessage {
    @NotNull
    @Property(name = "message")
    public String message;
}

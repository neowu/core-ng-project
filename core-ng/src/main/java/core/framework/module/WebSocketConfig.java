package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.HTTPIOHandler;
import core.framework.impl.web.websocket.WebSocketHandler;
import core.framework.web.websocket.ChannelListener;
import core.framework.web.websocket.WebSocketContext;

/**
 * @author neo
 */
public final class WebSocketConfig {
    private final ModuleContext context;

    WebSocketConfig(ModuleContext context) {
        this.context = context;
    }

    public void listen(String path, ChannelListener listener) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");

        if (context.httpServer.handler.webSocketHandler == null) {
            context.httpServer.handler.webSocketHandler = new WebSocketHandler(context.logManager);
            context.beanFactory.bind(WebSocketContext.class, null, context.httpServer.handler.webSocketHandler.context);
        }

        context.httpServer.handler.webSocketHandler.add(path, listener);
    }
}

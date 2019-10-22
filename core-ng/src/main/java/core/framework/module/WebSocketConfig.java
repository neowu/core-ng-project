package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.websocket.WebSocketHandler;
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
            context.httpServer.handler.webSocketHandler = new WebSocketHandler(context.logManager, context.httpServer.siteManager.sessionManager, context.httpServer.handler.responseBeanMapper);
            context.beanFactory.bind(WebSocketContext.class, null, context.httpServer.handler.webSocketHandler.context);
        }

        context.httpServer.handler.webSocketHandler.add(path, listener);
    }
}

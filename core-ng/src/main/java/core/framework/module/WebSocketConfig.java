package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.websocket.ChannelHandler;
import core.framework.internal.web.websocket.WebSocketHandler;
import core.framework.web.websocket.ChannelListener;
import core.framework.web.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public final class WebSocketConfig {
    final ModuleContext context;
    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    WebSocketConfig(ModuleContext context) {
        this.context = context;
    }

    public <T, V> void listen(String path, Class<T> clientMessageClass, Class<V> serverMessageClass, ChannelListener<T, V> listener) {
        logger.info("ws, path={}, clientMessageClass={}, serverMessageClass={}, listener={}",
                path, clientMessageClass.getCanonicalName(), serverMessageClass.getCanonicalName(), listener.getClass().getCanonicalName());

        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");

        if (context.httpServer.handler.webSocketHandler == null) {
            context.httpServer.handler.webSocketHandler = new WebSocketHandler(context.logManager, context.httpServer.siteManager.sessionManager, context.httpServer.handler.rateControl);
            context.beanFactory.bind(WebSocketContext.class, null, context.httpServer.handler.webSocketHandler.context);
        }

        context.beanClassValidator.validate(clientMessageClass);
        context.beanClassValidator.validate(serverMessageClass);
        context.serviceRegistry.beanClasses.add(clientMessageClass);
        context.serviceRegistry.beanClasses.add(serverMessageClass);

        var handler = new ChannelHandler<>(clientMessageClass, serverMessageClass, listener);
        context.httpServer.handler.webSocketHandler.add(path, handler);
    }
}

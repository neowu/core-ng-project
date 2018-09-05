package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.HealthCheckHandler;
import core.framework.util.Exceptions;
import core.framework.web.websocket.ChannelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public final class WebSocketConfig {
    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final ModuleContext context;

    WebSocketConfig(ModuleContext context) {
        this.context = context;
    }

    public void add(String path, ChannelListener listener) {
        if (HealthCheckHandler.PATH.equals(path)) throw new Error("/health-check is reserved path");
        if (path.contains("/:")) throw Exceptions.error("websocket path must be static, path={}", path);
        Class<? extends ChannelListener> listenerClass = listener.getClass();
        if (listenerClass.isSynthetic())
            throw Exceptions.error("listener class must not be anonymous class or lambda, please create static class, listenerClass={}", listenerClass.getCanonicalName());
        logger.info("ws, path={}, listener={}", path, listenerClass.getCanonicalName());
        ChannelListener previous = context.httpServer.handler.webSocketHandler.listeners.putIfAbsent(path, listener);
        if (previous != null) throw Exceptions.error("found duplicate ws listener, path={}, previousClass={}", previous.getClass().getCanonicalName());
    }
}

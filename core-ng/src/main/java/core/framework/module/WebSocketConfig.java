package core.framework.module;

import core.framework.internal.inject.InjectValidator;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.websocket.ChannelHandler;
import core.framework.internal.web.websocket.WebSocketHandler;
import core.framework.internal.web.websocket.WebSocketMetrics;
import core.framework.web.websocket.ChannelListener;
import core.framework.web.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author neo
 */
public final class WebSocketConfig extends Config {
    // use http().limitRate().add(WebSocketConfig.WS_OPEN_GROUP, ...) to configure rate limiting for ws connections
    public static final String WS_OPEN_GROUP = "ws:open";

    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, @Nullable String name) {
        this.context = context;
    }

    @Override
    protected void validate() {
        if (!context.httpServer.handler.rateControl.hasGroup(WS_OPEN_GROUP)) {
            context.httpServer.handler.rateControl.config(WS_OPEN_GROUP, 10, 10, Duration.ofSeconds(30));
        }
    }

    public <T, V> void listen(String path, Class<T> clientMessageClass, Class<V> serverMessageClass, ChannelListener<T, V> listener) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");
        if (path.contains("/:")) throw new Error("listener path must be static, path=" + path);

        if (listener.getClass().isSynthetic())
            throw new Error("listener class must not be anonymous class or lambda, please create static class, listenerClass=" + listener.getClass().getCanonicalName());
        new InjectValidator(listener).validate();

        logger.info("ws, path={}, clientMessageClass={}, serverMessageClass={}, listener={}",
            path, clientMessageClass.getCanonicalName(), serverMessageClass.getCanonicalName(), listener.getClass().getCanonicalName());

        if (context.httpServer.handler.webSocketHandler == null) {
            context.httpServer.handler.webSocketHandler = new WebSocketHandler(context.logManager, context.httpServer.siteManager.sessionManager, context.httpServer.handler.rateControl);
            context.beanFactory.bind(WebSocketContext.class, null, context.httpServer.handler.webSocketHandler.context);

            context.collector.metrics.add(new WebSocketMetrics(context.httpServer.handler.webSocketHandler));
        }

        context.beanClassValidator.validate(clientMessageClass);
        context.beanClassValidator.validate(serverMessageClass);
        context.apiController.beanClasses.add(clientMessageClass);
        context.apiController.beanClasses.add(serverMessageClass);

        var handler = new ChannelHandler<>(clientMessageClass, serverMessageClass, listener);
        context.httpServer.handler.webSocketHandler.add(path, handler);
    }
}

package core.framework.module;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.bean.BeanMapper;
import core.framework.internal.web.bean.BeanMappers;
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
    private final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final ModuleContext context;

    WebSocketConfig(ModuleContext context) {
        this.context = context;
    }

    public void listen(String path, ChannelListener<String> listener) {
        listen(path, String.class, String.class, listener);
    }

    public <T, V> void listen(String path, Class<T> clientMessageClass, Class<V> serverMessageClass, ChannelListener<T> listener) {
        logger.info("ws, path={}, clientMessageClass={}, serverMessageClass={}, listener={}",
            path, clientMessageClass.getCanonicalName(), serverMessageClass.getCanonicalName(), listener.getClass().getCanonicalName());

        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");

        BeanMappers beanMappers = context.httpServer.handler.beanMappers;
        if (context.httpServer.handler.webSocketHandler == null) {
            context.httpServer.handler.webSocketHandler = new WebSocketHandler(context.logManager, context.httpServer.siteManager.sessionManager);
            context.beanFactory.bind(WebSocketContext.class, null, context.httpServer.handler.webSocketHandler.context);
        }
        BeanClassNameValidator beanClassNameValidator = context.serviceRegistry.beanClassNameValidator;
        BeanMapper<T> clientMessageMapper = null;
        if (!String.class.equals(clientMessageClass)) {
            clientMessageMapper = beanMappers.register(clientMessageClass, beanClassNameValidator);
            context.serviceRegistry.beanClasses.add(clientMessageClass);
        }
        BeanMapper<V> serverMessageMapper = null;
        if (!String.class.equals(serverMessageClass)) {
            serverMessageMapper = beanMappers.register(serverMessageClass, beanClassNameValidator);
            context.serviceRegistry.beanClasses.add(serverMessageClass);
        }
        var handler = new ChannelHandler(clientMessageMapper, serverMessageClass, serverMessageMapper, listener);
        context.httpServer.handler.webSocketHandler.add(path, handler);
    }
}

package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.inject.InjectValidator;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.HTTPIOHandler;
import core.framework.internal.web.sse.ServerSentEventContextImpl;
import core.framework.internal.web.sse.ServerSentEventHandler;
import core.framework.internal.web.sse.ServerSentEventMetrics;
import core.framework.util.Types;
import core.framework.web.sse.ChannelListener;
import core.framework.web.sse.ServerSentEventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;

public class ServerSentEventConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(ServerSentEventConfig.class);

    ModuleContext context;
    private ServerSentEventMetrics metrics;

    @Override
    protected void initialize(ModuleContext context, @Nullable String name) {
        this.context = context;
    }

    public <T> void listen(HTTPMethod method, String path, Class<T> eventClass, ChannelListener<T> listener) {
        if (HTTPIOHandler.HEALTH_CHECK_PATH.equals(path)) throw new Error("/health-check is reserved path");
        if (path.contains("/:")) throw new Error("listener path must be static, path=" + path);

        if (listener.getClass().isSynthetic())
            throw new Error("listener class must not be anonymous class or lambda, please create static class, listenerClass=" + listener.getClass().getCanonicalName());
        new InjectValidator(listener).validate();

        logger.info("sse, method={}, path={}, eventClass={}, listener={}", method, path, eventClass.getCanonicalName(), listener.getClass().getCanonicalName());

        if (context.httpServer.sseHandler == null) {
            context.httpServer.sseHandler = new ServerSentEventHandler(context.logManager, context.httpServer.siteManager.sessionManager, context.httpServer.handlerContext);
            metrics = new ServerSentEventMetrics();
            context.collector.metrics.add(metrics);
        }

        context.beanClassValidator.validate(eventClass);
        context.apiController.beanClasses.add(eventClass);

        var sseContext = new ServerSentEventContextImpl<T>();
        context.httpServer.sseHandler.add(method, path, eventClass, listener, sseContext);
        context.beanFactory.bind(Types.generic(ServerSentEventContext.class, eventClass), null, sseContext);
        metrics.contexts.add(sseContext);
        context.backgroundTask().scheduleWithFixedDelay(sseContext::keepAlive, Duration.ofSeconds(15));
    }
}

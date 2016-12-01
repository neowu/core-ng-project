package core.framework.api.module;

import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Interceptor;
import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class HTTPConfig {
    private final ModuleContext context;

    public HTTPConfig(ModuleContext context) {
        this.context = context;
    }

    public void httpPort(int port) {
        context.httpServer.httpPort = port;
    }

    public void httpsPort(int port) {
        context.httpServer.httpsPort = port;
    }

    public void intercept(Interceptor interceptor) {
        context.httpServer.handler.interceptors.add(interceptor);
    }

    public void errorHandler(ErrorHandler handler) {
        context.httpServer.handler.errorHandler.customErrorHandler = handler;
    }
}

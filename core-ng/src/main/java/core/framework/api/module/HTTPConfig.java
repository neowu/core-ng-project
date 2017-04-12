package core.framework.api.module;

import core.framework.api.web.ErrorHandler;
import core.framework.api.web.Interceptor;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.rate.LimitRateInterceptor;

/**
 * @author neo
 */
public final class HTTPConfig {
    private final ModuleContext context;
    private final State state;

    public HTTPConfig(ModuleContext context) {
        this.context = context;
        state = context.config.http();
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

    public LimitRateConfig limitRate() {
        if (state.limitRateInterceptor == null) {
            state.limitRateInterceptor = new LimitRateInterceptor();
            intercept(state.limitRateInterceptor);
        }
        return new LimitRateConfig(state);
    }

    public static class State {
        LimitRateInterceptor limitRateInterceptor;
        boolean limitRateGroupAdded;

        public void validate() {
            if (limitRateInterceptor != null && !limitRateGroupAdded) {
                throw new Error("limitRate() is configured but no group added, please remove unnecessary config");
            }
        }
    }
}

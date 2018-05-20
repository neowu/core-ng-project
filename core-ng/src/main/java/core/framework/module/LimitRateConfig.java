package core.framework.module;

import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.http.LimitRateInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class LimitRateConfig extends Config {
    private LimitRateInterceptor interceptor;
    private boolean limitRateGroupAdded;

    @Override
    protected void initialize(ModuleContext context, String name) {
        interceptor = new LimitRateInterceptor();
        context.httpServer.handler.interceptors.add(interceptor);
    }

    @Override
    protected void validate() {
        if (!limitRateGroupAdded) {
            throw new Error("limitRate is configured but no group added, please remove unnecessary config");
        }
    }

    public void add(String group, int maxPermits, int fillRate, TimeUnit unit) {
        interceptor.config(group, maxPermits, fillRate, unit);
        limitRateGroupAdded = true;
    }
}

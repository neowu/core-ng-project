package core.framework.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.http.LimitRateInterceptor;
import core.framework.internal.web.http.RateControl;

import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class LimitRateConfig extends Config {
    private boolean rateControlGroupAdded;
    private RateControl rateControl;

    @Override
    protected void initialize(ModuleContext context, String name) {
        rateControl = context.httpServer.handler.rateControl;
        context.httpServer.handler.interceptors.add(new LimitRateInterceptor(rateControl));
    }

    @Override
    protected void validate() {
        if (!rateControlGroupAdded) {
            throw new Error("limitRate is configured but no group added, please remove unnecessary config");
        }
    }

    public void add(String group, int maxPermits, int fillRate, TimeUnit unit) {
        rateControl.config(group, maxPermits, fillRate, unit);
        rateControlGroupAdded = true;
    }
}

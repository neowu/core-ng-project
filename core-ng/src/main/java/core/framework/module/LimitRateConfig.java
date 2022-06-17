package core.framework.module;

import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.http.LimitRateInterceptor;
import core.framework.internal.web.http.RateControl;

import java.time.Duration;

/**
 * @author neo
 */
public final class LimitRateConfig extends Config {
    private boolean rateControlGroupAdded;
    private RateControl rateControl;

    @Override
    protected void initialize(ModuleContext context, String name) {
        rateControl = context.httpServer.handler.rateControl;
        // save at max 5K group/ip combination per pod, about 800K memory, to adapt with more ips/cc attack, better defense with cloud infra based solution together
        maxEntries(5000);
        context.httpServerConfig.interceptors.add(new LimitRateInterceptor(rateControl));
    }

    @Override
    protected void validate() {
        if (!rateControlGroupAdded) {
            throw new Error("limitRate is configured but no group added, please remove unnecessary config");
        }
    }

    // how many group/ip combinations to keep, 5000 is about 800K, 10K is about 1.8M
    public void maxEntries(int entries) {
        rateControl.maxEntries(entries);
    }

    // maintain maxPermits at most, fill permits by fillRate per interval
    // e.g. add("group", 10, 5, Duration.ofSeconds(1)) keeps 10 permits at most, fills 5 permits every second
    // e.g. add("group", 20, 10, Duration.ofMinutes(5)) keeps 20 permits at most, fills 10 permits every 5 minutes
    public void add(String group, int maxPermits, int fillRate, Duration interval) {
        rateControl.config(group, maxPermits, fillRate, interval);
        rateControlGroupAdded = true;
    }
}

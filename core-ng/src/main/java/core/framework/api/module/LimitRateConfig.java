package core.framework.api.module;

import core.framework.impl.web.rate.LimitRateInterceptor;

/**
 * @author neo
 */
public final class LimitRateConfig {
    private final LimitRateInterceptor interceptor;

    public LimitRateConfig(LimitRateInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void add(String group, int maxPermits, int fillRatePerSecond) {
        interceptor.config(group, maxPermits, fillRatePerSecond);
    }
}
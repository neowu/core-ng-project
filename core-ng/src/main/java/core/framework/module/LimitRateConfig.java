package core.framework.module;

import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class LimitRateConfig {
    private final HTTPConfig config;

    LimitRateConfig(HTTPConfig config) {
        this.config = config;
    }

    public void add(String group, int maxPermits, int fillRate, TimeUnit unit) {
        config.limitRateInterceptor.config(group, maxPermits, fillRate, unit);
        config.limitRateGroupAdded = true;
    }
}

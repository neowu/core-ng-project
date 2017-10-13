package core.framework.module;

import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public final class LimitRateConfig {
    private final HTTPConfig.State state;

    LimitRateConfig(HTTPConfig.State state) {
        this.state = state;
    }

    public void add(String group, int maxPermits, int fillRate, TimeUnit unit) {
        state.limitRateInterceptor.config(group, maxPermits, fillRate, unit);
        state.limitRateGroupAdded = true;
    }
}

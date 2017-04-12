package core.framework.api.module;

/**
 * @author neo
 */
public final class LimitRateConfig {
    private final HTTPConfig.State state;

    LimitRateConfig(HTTPConfig.State state) {
        this.state = state;
    }

    public void add(String group, int maxPermits, int fillRatePerSecond) {
        state.limitRateInterceptor.config(group, maxPermits, fillRatePerSecond);
        state.limitRateGroupAdded = true;
    }
}
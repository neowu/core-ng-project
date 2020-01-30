package app.monitor;

import core.framework.api.json.Property;
import core.framework.api.validate.Max;
import core.framework.api.validate.Min;
import core.framework.api.validate.NotNull;
import core.framework.api.validate.Size;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class MonitorConfig {
    @NotNull
    @Property(name = "redis")
    public Map<String, RedisConfig> redis = Map.of();

    public static class RedisConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "hosts")
        public List<String> hosts = List.of();

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highMemUsageThreshold")
        public Double highMemUsageThreshold = 0.8;
    }
}

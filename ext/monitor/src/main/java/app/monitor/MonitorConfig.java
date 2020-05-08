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

    @NotNull
    @Property(name = "es")
    public Map<String, ElasticSearchConfig> es = Map.of();

    @Property(name = "kube")
    public KubeConfig kube;

    public static class RedisConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "hosts")
        public List<String> hosts = List.of();

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highMemUsageThreshold")
        public Double highMemUsageThreshold = 0.7;
    }

    public static class ElasticSearchConfig {
        @NotNull
        @Property(name = "host")
        public String host;                             // only need to put one host per cluster

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highDiskUsageThreshold")
        public Double highDiskUsageThreshold = 0.7;

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highHeapUsageThreshold")
        public Double highHeapUsageThreshold = 0.8;     // with ES default setting, it generally does full GC at 75%
    }

    public static class KubeConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "namespaces")
        public List<String> namespaces = List.of();
    }
}

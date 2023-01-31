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

    @NotNull
    @Property(name = "kafka")
    public Map<String, KafkaConfig> kafka = Map.of();

    @NotNull
    @Property(name = "mongo")
    public Map<String, MongoConfig> mongo = Map.of();

    @Property(name = "kube")
    public KubeConfig kube;

    @Property(name = "api")
    public APIConfig api;

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
        @Property(name = "highCPUUsageThreshold")
        public Double highCPUUsageThreshold = 0.5;

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

    public static class KafkaConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "hosts")
        public List<String> hosts = List.of();

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highHeapUsageThreshold")
        public Double highHeapUsageThreshold = 0.85;    // with Kafka default setting, it generally does GC at 80%

        @NotNull
        @Min(0)
        @Property(name = "highDiskSizeThreshold")
        public Long highDiskSizeThreshold = 50_000_000_000L;    // use 50G as default, kafka jmx doesn't provide disk limit, usually in cloud/kube env, pod pvc disk usage can be monitored by cloud monitoring as well
    }

    public static class MongoConfig {
        @NotNull
        @Property(name = "host")
        public String host;

        @NotNull
        @Min(0)
        @Max(1)
        @Property(name = "highDiskUsageThreshold")
        public Double highDiskUsageThreshold = 0.7;
    }

    public static class KubeConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "namespaces")
        public List<String> namespaces = List.of();
    }

    public static class APIConfig {
        @NotNull
        @Size(min = 1)
        @Property(name = "services")
        public List<String> services = List.of();
    }
}

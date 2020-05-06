package app;

import app.monitor.MonitorConfig;
import app.monitor.job.ElasticSearchCollector;
import app.monitor.job.KubeMonitorJob;
import app.monitor.job.MonitorJob;
import app.monitor.job.RedisCollector;
import app.monitor.kube.KubeClient;
import core.framework.http.HTTPClient;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.Module;
import core.framework.redis.Redis;

import java.time.Duration;
import java.util.Map;

/**
 * @author neo
 */
public class MonitorModule extends Module {
    @Override
    protected void initialize() {
        property("app.monitor.config").ifPresent(this::configureMonitor);
    }

    private void configureMonitor(String monitorConfig) {
        Bean.register(MonitorConfig.class);
        MonitorConfig config = Bean.fromJSON(MonitorConfig.class, monitorConfig);
        MessagePublisher<StatMessage> publisher = kafka().publish(LogTopics.TOPIC_STAT, StatMessage.class);
        if (!config.redis.isEmpty()) {
            configureRedisJob(publisher, config.redis);
        }
        if (!config.es.isEmpty()) {
            configureESJob(publisher, config.es);
        }
        if (config.kube != null) {
            configureKubeJob(publisher, config.kube);
        }
    }

    private void configureKubeJob(MessagePublisher<StatMessage> publisher, MonitorConfig.KubeConfig config) {
        KubeClient kubeClient = bind(new KubeClient());
        kubeClient.initialize();
        var job = new KubeMonitorJob(publisher, kubeClient, config.namespaces);
        schedule().fixedRate("kube", job, Duration.ofSeconds(30));  // not check pod too often
    }

    private void configureESJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.ElasticSearchConfig> config) {
        HTTPClient httpClient = HTTPClient.builder().build();
        for (Map.Entry<String, MonitorConfig.ElasticSearchConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.ElasticSearchConfig esConfig = entry.getValue();
            for (String host : esConfig.hosts) {
                var collector = new ElasticSearchCollector(httpClient, host);
                collector.highHeapUsageThreshold = esConfig.highHeapUsageThreshold;
                collector.highDiskUsageThreshold = esConfig.highDiskUsageThreshold;
                schedule().fixedRate("es-" + host, new MonitorJob(collector, app, host, publisher), Duration.ofSeconds(10));
            }
        }
    }

    private void configureRedisJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.RedisConfig> config) {
        for (Map.Entry<String, MonitorConfig.RedisConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.RedisConfig redisConfig = entry.getValue();
            for (String host : redisConfig.hosts) {
                redis(host).host(host);
                redis(host).poolSize(1, 1);
                Redis redis = redis(host).client();
                var collector = new RedisCollector(redis);
                collector.highMemUsageThreshold = redisConfig.highMemUsageThreshold;
                schedule().fixedRate("redis-" + host, new MonitorJob(collector, app, host, publisher), Duration.ofSeconds(10));
            }
        }
    }
}

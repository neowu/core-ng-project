package app;

import app.monitor.MonitorConfig;
import app.monitor.job.APIMonitorJob;
import app.monitor.job.ElasticSearchClient;
import app.monitor.job.ElasticSearchMonitorJob;
import app.monitor.job.JMXClient;
import app.monitor.job.KafkaMonitorJob;
import app.monitor.job.KubeClient;
import app.monitor.job.KubeMonitorJob;
import app.monitor.job.MongoMonitorJob;
import app.monitor.job.RedisMonitorJob;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.framework.http.HTTPClient;
import core.framework.internal.log.LogManager;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.Module;
import core.framework.redis.Redis;
import core.framework.util.Strings;

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
        if (!config.kafka.isEmpty()) {
            configureKafkaJob(publisher, config.kafka);
        }
        if (!config.mongo.isEmpty()) {
            configureMongoJob(publisher, config.mongo);
        }
        if (config.kube != null) {
            configureKubeJob(publisher, config.kube);
        }
        if (config.api != null) {
            configureAPIJob(publisher, config.api);
        }
    }

    private void configureAPIJob(MessagePublisher<StatMessage> publisher, MonitorConfig.APIConfig config) {
        HTTPClient httpClient = HTTPClient.builder().userAgent("monitor").trustAll().build();
        var job = new APIMonitorJob(httpClient, config.services, publisher);
        schedule().fixedRate("monitor:api", job, Duration.ofMinutes(10));  // not check api too often
    }

    private void configureKubeJob(MessagePublisher<StatMessage> publisher, MonitorConfig.KubeConfig config) {
        KubeClient kubeClient = bind(new KubeClient());
        kubeClient.initialize();
        var job = new KubeMonitorJob(config.namespaces, kubeClient, publisher);
        schedule().fixedRate("monitor:kube", job, Duration.ofSeconds(30));  // not check pod too often
    }

    private void configureESJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.ElasticSearchConfig> config) {
        var elasticSearchClient = new ElasticSearchClient();
        for (Map.Entry<String, MonitorConfig.ElasticSearchConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.ElasticSearchConfig esConfig = entry.getValue();

            var job = new ElasticSearchMonitorJob(elasticSearchClient, app, esConfig.host, publisher);
            job.highCPUUsageThreshold = esConfig.highCPUUsageThreshold;
            job.highHeapUsageThreshold = esConfig.highHeapUsageThreshold;
            job.highDiskUsageThreshold = esConfig.highDiskUsageThreshold;
            schedule().fixedRate("monitor:es:" + app, job, Duration.ofSeconds(10));
        }
    }

    private void configureMongoJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.MongoConfig> config) {
        for (Map.Entry<String, MonitorConfig.MongoConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.MongoConfig mongoConfig = entry.getValue();

            String connectionString = Strings.format("mongodb://{}/?socketTimeoutMS=10000&serverSelectionTimeoutMS=0&appName={}", mongoConfig.host, LogManager.APP_NAME);
            MongoClient client = MongoClients.create(new ConnectionString(connectionString));

            var job = new MongoMonitorJob(client, app, mongoConfig.host, publisher);
            job.highDiskUsageThreshold = mongoConfig.highDiskUsageThreshold;
            schedule().fixedRate("monitor:mongo:" + mongoConfig.host, job, Duration.ofSeconds(30));
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
                var job = new RedisMonitorJob(redis, app, host, publisher);
                job.highMemUsageThreshold = redisConfig.highMemUsageThreshold;
                schedule().fixedRate("monitor:redis:" + host, job, Duration.ofSeconds(10));
            }
        }
    }

    private void configureKafkaJob(MessagePublisher<StatMessage> publisher, Map<String, MonitorConfig.KafkaConfig> config) {
        for (Map.Entry<String, MonitorConfig.KafkaConfig> entry : config.entrySet()) {
            String app = entry.getKey();
            MonitorConfig.KafkaConfig kafkaConfig = entry.getValue();
            for (String host : kafkaConfig.hosts) {
                var job = new KafkaMonitorJob(new JMXClient(host), app, host, publisher);
                job.highHeapUsageThreshold = kafkaConfig.highHeapUsageThreshold;
                job.highDiskSizeThreshold = kafkaConfig.highDiskSizeThreshold;
                schedule().fixedRate("monitor:kafka:" + host, job, Duration.ofSeconds(10));
            }
        }
    }
}

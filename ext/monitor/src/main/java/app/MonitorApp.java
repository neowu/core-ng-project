package app;

import app.monitor.AlertConfig;
import app.monitor.MonitorConfig;
import app.monitor.alert.AlertService;
import app.monitor.job.MonitorJob;
import app.monitor.job.RedisCollector;
import app.monitor.kafka.ActionLogMessageHandler;
import app.monitor.kafka.EventMessageHandler;
import app.monitor.kafka.StatMessageHandler;
import app.monitor.slack.SlackClient;
import app.monitor.slack.SlackMessageAPIRequest;
import app.monitor.slack.SlackMessageAPIResponse;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.json.Bean;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.redis.Redis;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * @author ericchung
 */
public class MonitorApp extends App {
    public static final String MONITOR_APP = "monitor";

    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");

        configureSlackClient(); // currently only support slack notification

        configureAlert();

        configureMonitor();
    }

    private void configureMonitor() {
        Optional<String> monitorConfigJSON = property("app.monitor.config");
        if (monitorConfigJSON.isEmpty()) return;

        Bean.register(MonitorConfig.class);
        MonitorConfig config = Bean.fromJSON(MonitorConfig.class, monitorConfigJSON.get());
        if (!config.redis.isEmpty()) {
            MessagePublisher<StatMessage> publisher = kafka().publish(LogTopics.TOPIC_STAT, StatMessage.class);
            configureRedisJob(publisher, config.redis);
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
                schedule().fixedRate("redis-" + host, new MonitorJob(new RedisCollector(redis, redisConfig.highMemUsageThreshold), app, host, publisher), Duration.ofSeconds(10));
            }
        }
    }

    private void configureAlert() {
        Bean.register(AlertConfig.class);
        AlertConfig config = Bean.fromJSON(AlertConfig.class, requiredProperty("app.alert.config"));
        bind(new AlertService(config));
        kafka().poolSize(Runtime.getRuntime().availableProcessors() == 1 ? 1 : 2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get 1M message
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));
    }

    private void configureSlackClient() {
        bind(HTTPClient.class, new HTTPClientBuilder().build());

        Bean.register(SlackMessageAPIRequest.class);
        Bean.register(SlackMessageAPIResponse.class);
        bind(new SlackClient(requiredProperty("app.slack.token")));
    }
}

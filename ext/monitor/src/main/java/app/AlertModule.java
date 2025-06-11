package app;

import app.monitor.AlertConfig;
import app.monitor.alert.AlertService;
import app.monitor.channel.SlackClient;
import app.monitor.kafka.ActionLogMessageHandler;
import app.monitor.kafka.EventMessageHandler;
import app.monitor.kafka.StatMessageHandler;
import core.framework.http.HTTPClient;
import core.framework.json.Bean;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.log.message.StatMessage;
import core.framework.module.Module;

import java.time.Duration;

/**
 * @author neo
 */
public class AlertModule extends Module {
    @Override
    protected void initialize() {
        property("app.alert.config").ifPresent(this::configureAlert);
    }

    private void configureSlack() {
        final String token = requiredProperty("app.slack.token");
        HTTPClient httpClient = HTTPClient.builder()
            .maxRetries(3)
            .retryWaitTime(Duration.ofSeconds(2))    // slack has rate limit with 1 message per second, here to slow down further when hit limit, refer to https://api.slack.com/docs/rate-limits
            .build();
        bind(new SlackClient(httpClient, token));
    }

    private void configureAlert(String alertConfig) {
        configureSlack();

        Bean.register(AlertConfig.class);
        AlertConfig config = Bean.fromJSON(AlertConfig.class, alertConfig);
        bind(new AlertService(config));

        kafka().concurrency(2);
        kafka().minPoll(1024 * 1024, Duration.ofMillis(500));           // try to get 1M message
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_STAT, StatMessage.class, bind(StatMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));
    }
}

package app;

import app.monitor.AlertConfig;
import app.monitor.alert.AlertService;
import app.monitor.kafka.ActionLogMessageHandler;
import app.monitor.kafka.EventMessageHandler;
import app.monitor.slack.SlackClient;
import app.monitor.slack.SlackMessageAPIRequest;
import app.monitor.slack.SlackMessageAPIResponse;
import core.framework.http.HTTPClient;
import core.framework.http.HTTPClientBuilder;
import core.framework.json.Bean;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.module.App;
import core.framework.module.SystemModule;

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

        configureActionAlert();
    }

    private void configureActionAlert() {
        Bean.register(AlertConfig.class);
        AlertConfig config = Bean.fromJSON(AlertConfig.class, requiredProperty("app.alert.config"));
        bind(new AlertService(config));
        kafka().subscribe(LogTopics.TOPIC_ACTION_LOG, ActionLogMessage.class, bind(ActionLogMessageHandler.class));
        kafka().subscribe(LogTopics.TOPIC_EVENT, EventMessage.class, bind(EventMessageHandler.class));
    }

    private void configureSlackClient() {
        bind(HTTPClient.class, new HTTPClientBuilder().build());

        Bean.register(SlackMessageAPIRequest.class);
        Bean.register(SlackMessageAPIResponse.class);
        bind(new SlackClient(requiredProperty("app.slack.token")));
    }
}

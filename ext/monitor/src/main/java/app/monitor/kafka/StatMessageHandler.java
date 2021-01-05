package app.monitor.kafka;

import app.monitor.alert.Alert;
import app.monitor.alert.AlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.message.StatMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author neo
 */
public class StatMessageHandler implements MessageHandler<StatMessage> {
    @Inject
    AlertService alertService;

    @Override
    public void handle(String key, StatMessage message) {
        if (message.errorCode == null) return;

        alertService.process(alert(message));
    }

    private Alert alert(StatMessage message) {
        var alert = new Alert();
        alert.id = message.id;
        alert.date = LocalDateTime.ofInstant(message.date, ZoneId.systemDefault());
        alert.app = message.app;
        alert.severity(message.result);
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "stat";
        alert.host = message.host;
        return alert;
    }
}

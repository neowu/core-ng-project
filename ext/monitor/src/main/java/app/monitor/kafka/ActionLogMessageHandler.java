package app.monitor.kafka;

import app.monitor.alert.Alert;
import app.monitor.alert.AlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.message.ActionLogMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author ericchung
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    static final String MONITOR_APP = "monitor";

    @Inject
    AlertService alertService;

    @Override
    public void handle(String key, ActionLogMessage message) {
        if (MONITOR_APP.equals(message.app)) return; // ignore self action to avoid infinite loop on error
        if (message.errorCode == null) return;

        alertService.process(alert(message));
    }

    private Alert alert(ActionLogMessage message) {
        var alert = new Alert();
        alert.id = message.id;
        alert.date = LocalDateTime.ofInstant(message.date, ZoneId.systemDefault());
        alert.app = message.app;
        alert.action = message.action;
        alert.severity(message.result);
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "trace";
        return alert;
    }
}

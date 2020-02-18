package app.monitor.kafka;

import app.MonitorApp;
import app.monitor.alert.Alert;
import app.monitor.alert.AlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.message.ActionLogMessage;

/**
 * @author ericchung
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    @Inject
    AlertService alertService;

    @Override
    public void handle(String key, ActionLogMessage message) {
        if (MonitorApp.MONITOR_APP.equals(message.app)) return; // ignore self action to avoid infinite loop on error
        if (message.errorCode == null) return;

        alertService.process(alert(message));
    }

    private Alert alert(ActionLogMessage message) {
        var alert = new Alert();
        alert.id = message.id;
        alert.app = message.app;
        alert.action = message.action;
        alert.severity(message.result);
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "trace";
        return alert;
    }
}

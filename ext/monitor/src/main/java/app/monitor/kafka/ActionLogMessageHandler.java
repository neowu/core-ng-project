package app.monitor.kafka;

import app.MonitorApp;
import app.monitor.action.ActionAlert;
import app.monitor.action.ActionAlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.message.ActionLogMessage;

/**
 * @author ericchung
 */
public class ActionLogMessageHandler implements MessageHandler<ActionLogMessage> {
    @Inject
    ActionAlertService actionAlertService;

    @Override
    public void handle(String key, ActionLogMessage message) {
        if (MonitorApp.MONITOR_APP.equals(message.app)) return; // ignore self action
        if (message.errorCode == null) return;

        actionAlertService.process(alert(message));
    }

    private ActionAlert alert(ActionLogMessage message) {
        var alert = new ActionAlert();
        alert.id = message.id;
        alert.app = message.app;
        alert.severity(message.result);
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "trace";
        return alert;
    }
}

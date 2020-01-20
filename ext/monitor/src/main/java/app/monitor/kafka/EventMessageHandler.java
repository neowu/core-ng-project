package app.monitor.kafka;

import app.monitor.action.ActionAlert;
import app.monitor.action.ActionAlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.Severity;
import core.framework.log.message.EventMessage;

/**
 * @author ericchung
 */
public class EventMessageHandler implements MessageHandler<EventMessage> {
    @Inject
    ActionAlertService actionAlertService;

    @Override
    public void handle(String key, EventMessage message) {
        if (message.errorCode == null) return;

        actionAlertService.process(alert(message));
    }

    private ActionAlert alert(EventMessage message) {
        var alert = new ActionAlert();
        alert.id = message.id;
        alert.app = message.app;
        alert.severity = "WARN".equals(message.result) ? Severity.WARN : Severity.ERROR;
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "event";
        return alert;
    }
}

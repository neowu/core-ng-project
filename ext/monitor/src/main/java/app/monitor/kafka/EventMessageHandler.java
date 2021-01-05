package app.monitor.kafka;

import app.monitor.alert.Alert;
import app.monitor.alert.AlertService;
import core.framework.inject.Inject;
import core.framework.kafka.MessageHandler;
import core.framework.log.message.EventMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author ericchung
 */
public class EventMessageHandler implements MessageHandler<EventMessage> {
    @Inject
    AlertService alertService;

    @Override
    public void handle(String key, EventMessage message) {
        if (message.errorCode == null) return;

        alertService.process(alert(message));
    }

    private Alert alert(EventMessage message) {
        var alert = new Alert();
        alert.id = message.id;
        alert.date = LocalDateTime.ofInstant(message.date, ZoneId.systemDefault());
        alert.app = message.app;
        alert.action = message.action;
        alert.severity(message.result);
        alert.errorCode = message.errorCode;
        alert.errorMessage = message.errorMessage;
        alert.kibanaIndex = "event";
        return alert;
    }
}

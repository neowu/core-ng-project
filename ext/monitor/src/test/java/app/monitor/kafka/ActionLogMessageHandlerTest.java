package app.monitor.kafka;

import app.MonitorApp;
import app.monitor.action.ActionAlertService;
import core.framework.log.Severity;
import core.framework.log.message.ActionLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author neo
 */
class ActionLogMessageHandlerTest {
    private ActionLogMessageHandler handler;
    private ActionAlertService actionAlertService;

    @BeforeEach
    void createActionLogMessageHandler() {
        handler = new ActionLogMessageHandler();
        actionAlertService = mock(ActionAlertService.class);
        handler.actionAlertService = actionAlertService;
    }

    @Test
    void handleSelfAction() {
        var message = new ActionLogMessage();
        message.app = MonitorApp.MONITOR_APP;
        handler.handle(null, message);
        verifyNoInteractions(actionAlertService);
    }

    @Test
    void handleOKAction() {
        var message = new ActionLogMessage();
        message.result = "OK";
        message.errorCode = null;
        handler.handle(null, message);
        verifyNoInteractions(actionAlertService);
    }

    @Test
    void handle() {
        var message = new ActionLogMessage();
        message.result = "WARN";
        message.errorCode = "NOT_FOUND";
        handler.handle(null, message);

        verify(actionAlertService).process(argThat(alert -> alert.severity == Severity.WARN));
    }
}

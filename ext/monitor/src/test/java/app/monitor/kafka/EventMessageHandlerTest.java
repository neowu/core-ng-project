package app.monitor.kafka;

import app.monitor.action.ActionAlertService;
import core.framework.log.Severity;
import core.framework.log.message.EventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author neo
 */
class EventMessageHandlerTest {
    private EventMessageHandler handler;
    private ActionAlertService actionAlertService;

    @BeforeEach
    void createEventMessageHandler() {
        handler = new EventMessageHandler();
        actionAlertService = mock(ActionAlertService.class);
        handler.actionAlertService = actionAlertService;
    }

    @Test
    void handleOKAction() {
        var message = new EventMessage();
        message.result = "OK";
        message.errorCode = null;
        handler.handle(null, message);
        verifyNoInteractions(actionAlertService);
    }

    @Test
    void handle() {
        var message = new EventMessage();
        message.result = "ERROR";
        message.errorCode = "RUNTIME_ERROR";
        handler.handle(null, message);

        verify(actionAlertService).process(argThat(alert -> alert.severity == Severity.ERROR));
    }
}

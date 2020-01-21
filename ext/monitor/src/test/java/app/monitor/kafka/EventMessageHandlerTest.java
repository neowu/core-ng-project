package app.monitor.kafka;

import app.monitor.alert.AlertService;
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
    private AlertService alertService;

    @BeforeEach
    void createEventMessageHandler() {
        handler = new EventMessageHandler();
        alertService = mock(AlertService.class);
        handler.alertService = alertService;
    }

    @Test
    void handleOKAction() {
        var message = new EventMessage();
        message.result = "OK";
        message.errorCode = null;
        handler.handle(null, message);
        verifyNoInteractions(alertService);
    }

    @Test
    void handle() {
        var message = new EventMessage();
        message.result = "ERROR";
        message.errorCode = "RUNTIME_ERROR";
        handler.handle(null, message);

        verify(alertService).process(argThat(alert -> alert.severity == Severity.ERROR));
    }
}

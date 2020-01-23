package app.monitor.kafka;

import app.monitor.alert.AlertService;
import core.framework.log.Severity;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author neo
 */
class StatMessageHandlerTest {
    private StatMessageHandler handler;
    private AlertService alertService;

    @BeforeEach
    void createStatMessageHandler() {
        handler = new StatMessageHandler();
        alertService = mock(AlertService.class);
        handler.alertService = alertService;
    }

    @Test
    void handleOKAction() {
        var message = new StatMessage();
        message.result = "OK";
        message.errorCode = null;
        handler.handle(null, message);
        verifyNoInteractions(alertService);
    }

    @Test
    void handle() {
        var message = new StatMessage();
        message.result = "WARN";
        message.errorCode = "HIGH_CPU_USAGE";
        handler.handle(null, message);

        verify(alertService).process(argThat(alert -> alert.severity == Severity.WARN));
    }
}

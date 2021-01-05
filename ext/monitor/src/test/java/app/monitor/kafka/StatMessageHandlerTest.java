package app.monitor.kafka;

import app.monitor.alert.AlertService;
import core.framework.log.Severity;
import core.framework.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class StatMessageHandlerTest {
    private StatMessageHandler handler;
    @Mock
    private AlertService alertService;

    @BeforeEach
    void createStatMessageHandler() {
        handler = new StatMessageHandler();
        handler.alertService = alertService;
    }

    @Test
    void handleOKAction() {
        var message = new StatMessage();
        message.date = Instant.now();
        message.result = "OK";
        message.errorCode = null;
        handler.handle(null, message);
        verifyNoInteractions(alertService);
    }

    @Test
    void handle() {
        var message = new StatMessage();
        message.date = Instant.now();
        message.result = "WARN";
        message.errorCode = "HIGH_CPU_USAGE";
        handler.handle(null, message);

        verify(alertService).process(argThat(alert -> alert.severity == Severity.WARN));
    }
}

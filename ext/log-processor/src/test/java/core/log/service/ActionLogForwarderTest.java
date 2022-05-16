package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ActionLogForwarderTest {
    @Mock
    MessagePublisher<ActionLogMessage> publisher;

    @Test
    void forwardWithMatchedApp() {
        var forwarder = new ActionLogForwarder(publisher, "action", List.of("website"), List.of(), List.of());

        var message = new ActionLogMessage();
        message.app = "website";
        message.traceLog = "trace";
        forwarder.forward(List.of(message));
        verify(publisher).publish("action", null, message);
    }

    @Test
    void forwardWithMatchedResult() {
        var forwarder = new ActionLogForwarder(publisher, "action", List.of("website"), List.of("OK"), List.of());

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "OK";
        message.traceLog = "trace";
        forwarder.forward(List.of(message));
        verify(publisher).publish("action", null, message);
    }

    @Test
    void forwardWithMismatchedResult() {
        var forwarder = new ActionLogForwarder(publisher, "action", List.of("website"), List.of("OK"), List.of());

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "WARN";
        forwarder.forward(List.of(message));
        verifyNoInteractions(publisher);
    }

    @Test
    void forwardWithIgnoredErrorCode() {
        var forwarder = new ActionLogForwarder(publisher, "action", List.of("website"), List.of(), List.of("NOT_FOUND"));

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "WARN";
        message.errorCode = "NOT_FOUND";
        forwarder.forward(List.of(message));
        verifyNoInteractions(publisher);
    }
}

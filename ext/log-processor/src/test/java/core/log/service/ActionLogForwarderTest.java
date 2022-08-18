package core.log.service;

import core.framework.kafka.Message;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.log.LogForwardConfig;
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
        var forwarder = new ActionLogForwarder(publisher, forward(List.of("website"), List.of(), List.of()));

        var message = new ActionLogMessage();
        message.app = "website";
        message.traceLog = "trace";
        forwarder.forward(List.of(new Message<>("k1", message)));
        verify(publisher).publish(message);
    }

    @Test
    void forwardWithMatchedResult() {
        var forwarder = new ActionLogForwarder(publisher, forward(List.of("website"), List.of("OK"), List.of()));

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "OK";
        message.traceLog = "trace";
        forwarder.forward(List.of(new Message<>("k1", message)));
        verify(publisher).publish(message);
    }

    @Test
    void forwardWithMismatchedResult() {
        var forwarder = new ActionLogForwarder(publisher, forward(List.of("website"), List.of("OK"), List.of()));

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "WARN";
        forwarder.forward(List.of(new Message<>("k1", message)));
        verifyNoInteractions(publisher);
    }

    @Test
    void forwardWithIgnoredErrorCode() {
        var forwarder = new ActionLogForwarder(publisher, forward(List.of("website"), List.of(), List.of("NOT_FOUND")));

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "WARN";
        message.errorCode = "NOT_FOUND";
        forwarder.forward(List.of(new Message<>("k1", message)));
        verifyNoInteractions(publisher);
    }

    @Test
    void forwardWithIgnoredAction() {
        LogForwardConfig.Forward forward = forward(List.of("website"), List.of(), List.of());
        forward.ignoreActions = List.of("api:get:/ajax/current-customer");
        var forwarder = new ActionLogForwarder(publisher, forward);

        var message = new ActionLogMessage();
        message.app = "website";
        message.result = "OK";
        message.action = "api:get:/ajax/current-customer";
        forwarder.forward(List.of(new Message<>("k1", message)));
        verifyNoInteractions(publisher);
    }

    private LogForwardConfig.Forward forward(List<String> apps, List<String> results, List<String> ignoreErrorCodes) {
        LogForwardConfig.Forward forward = new LogForwardConfig.Forward();
        forward.topic = "action";
        forward.apps = apps;
        forward.results = results;
        forward.ignoreErrorCodes = ignoreErrorCodes;
        return forward;
    }
}

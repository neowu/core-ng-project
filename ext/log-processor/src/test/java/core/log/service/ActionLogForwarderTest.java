package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ActionLogForwarderTest {
    @Mock
    MessagePublisher<ActionLogMessage> publisher;
    private ActionLogForwarder forwarder;

    @BeforeEach
    void createActionLogForwarder() {
        forwarder = new ActionLogForwarder(publisher, "action", List.of("website"), List.of());
    }

    @Test
    void forward() {
        var message = new ActionLogMessage();
        message.app = "website";
        message.traceLog = "trace";
        forwarder.forward(List.of(message));
        verify(publisher).publish("action", null, message);
    }
}

package core.log.service;

import core.framework.kafka.Message;
import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;
import core.log.LogForwardConfig;
import org.junit.jupiter.api.BeforeEach;
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
class EventForwarderTest {
    @Mock
    MessagePublisher<EventMessage> publisher;
    private EventForwarder forwarder;

    @BeforeEach
    void createEventForwarder() {
        var forward = new LogForwardConfig.Forward();
        forward.topic = "event";
        forward.apps = List.of("website");
        forward.ignoreErrorCodes = List.of("NOT_FOUND");
        forwarder = new EventForwarder(publisher, forward);
    }

    @Test
    void forward() {
        var message = new EventMessage();
        message.app = "website";
        forwarder.forward(List.of(new Message<>("k1", message)));

        verify(publisher).publish(message);
    }

    @Test
    void forwardWithIgnoreErrorCode() {
        var message = new EventMessage();
        message.app = "website";
        message.errorCode = "NOT_FOUND";
        forwarder.forward(List.of(new Message<>("k1", message)));

        verifyNoInteractions(publisher);
    }
}

package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;
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
        forwarder = new EventForwarder(publisher, "event", List.of("website"), List.of(), List.of("NOT_FOUND"));
    }

    @Test
    void forward() {
        var message = new EventMessage();
        message.app = "website";
        forwarder.forward(List.of(message));

        verify(publisher).publish("event", null, message);
    }

    @Test
    void forwardWithIgnoreErrorCode() {
        var message = new EventMessage();
        message.app = "website";
        message.errorCode = "NOT_FOUND";
        forwarder.forward(List.of(message));

        verifyNoInteractions(publisher);
    }
}

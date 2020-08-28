package core.log.kafka;

import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.log.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class EventMessageHandlerTest {
    @Mock
    EventService eventService;

    private EventMessageHandler handler;

    @BeforeEach
    void createEventMessageHandler() {
        handler = new EventMessageHandler();
        handler.eventService = eventService;
    }

    @Test
    void handle() {
        List<Message<EventMessage>> messages = List.of(new Message<>("k1", new EventMessage()), new Message<>("k1", new EventMessage()));
        handler.handle(messages);

        verify(eventService).index(argThat((List<EventMessage> values) -> values.size() == 2));
    }
}

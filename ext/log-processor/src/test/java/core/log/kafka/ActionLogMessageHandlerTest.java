package core.log.kafka;

import core.framework.kafka.Message;
import core.framework.log.message.ActionLogMessage;
import core.log.service.ActionLogForwarder;
import core.log.service.ActionService;
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
class ActionLogMessageHandlerTest {
    @Mock
    ActionService actionService;
    @Mock
    ActionLogForwarder forwarder;

    private ActionLogMessageHandler handler;

    @BeforeEach
    void createActionLogMessageHandler() {
        handler = new ActionLogMessageHandler(forwarder);
        handler.actionService = actionService;
    }

    @Test
    void handle() {
        List<Message<ActionLogMessage>> messages = List.of(new Message<>("k1", new ActionLogMessage()), new Message<>("k1", new ActionLogMessage()));
        handler.handle(messages);

        verify(actionService).index(argThat((List<ActionLogMessage> values) -> values.size() == 2));
        verify(forwarder).forward(argThat((List<ActionLogMessage> values) -> values.size() == 2));
    }
}

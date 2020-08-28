package core.log.kafka;

import core.framework.kafka.Message;
import core.framework.log.message.StatMessage;
import core.log.service.StatService;
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
class StatMessageHandlerTest {
    @Mock
    StatService statService;

    private StatMessageHandler handler;

    @BeforeEach
    void createStatMessageHandler() {
        handler = new StatMessageHandler();
        handler.statService = statService;
    }

    @Test
    void handle() {
        List<Message<StatMessage>> messages = List.of(new Message<>("k1", new StatMessage()), new Message<>("k1", new StatMessage()));
        handler.handle(messages);

        verify(statService).index(argThat(((List<StatMessage> values) -> values.size() == 2)));
    }
}

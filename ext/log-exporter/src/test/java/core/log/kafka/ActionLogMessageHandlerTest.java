package core.log.kafka;

import core.framework.kafka.Message;
import core.framework.log.message.ActionLogMessage;
import core.framework.util.Files;
import core.log.service.ArchiveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ActionLogMessageHandlerTest {
    private ActionLogMessageHandler handler;

    @BeforeEach
    void createActionLogMessageHandler() {
        handler = new ActionLogMessageHandler();
        handler.logDir = Files.tempDir();
        handler.archiveService = new ArchiveService();
    }

    @AfterEach
    void cleanup() {
        Files.deleteDir(handler.logDir);
    }

    @Test
    void handle() throws IOException {
        var message = new ActionLogMessage();
        message.date = Instant.parse("2022-11-07T00:00:00Z");
        message.id = "id";
        message.app = "app";
        message.traceLog = "trace";
        handler.handle(List.of(new Message<>("key", message)));
    }
}

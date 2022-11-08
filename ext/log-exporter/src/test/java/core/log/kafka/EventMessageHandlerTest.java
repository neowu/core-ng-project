package core.log.kafka;

import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.framework.util.Files;
import core.log.service.ArchiveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * @author neo
 */
class EventMessageHandlerTest {
    private EventMessageHandler handler;

    @BeforeEach
    void createEventMessageHandler() {
        handler = new EventMessageHandler();
        handler.archiveService = new ArchiveService();
        handler.archiveService.logDir = Files.tempDir();
    }

    @AfterEach
    void cleanup() {
        Files.deleteDir(handler.archiveService.logDir);
    }

    @Test
    void handle() throws IOException {
        var message = new EventMessage();
        message.date = Instant.parse("2022-11-07T00:00:00Z");
        message.id = "id";
        message.app = "app";
        handler.handle(List.of(new Message<>("key", message)));
    }
}

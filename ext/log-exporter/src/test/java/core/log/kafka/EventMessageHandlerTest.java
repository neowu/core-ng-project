package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.framework.util.Files;
import core.log.IntegrationTest;
import core.log.domain.EventSchema;
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
class EventMessageHandlerTest extends IntegrationTest {
    private EventMessageHandler handler;
    @Inject
    EventSchema schema;

    @BeforeEach
    void createEventMessageHandler() {
        handler = new EventMessageHandler();
        handler.archiveService = new ArchiveService();
        handler.archiveService.logDir = Files.tempDir();
        handler.schema = schema;
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
        message.action = "action";
        message.result = "OK";
        message.elapsed = 1000L;
        handler.handle(List.of(new Message<>("key", message)));
        handler.handle(List.of(new Message<>("key", message)));
    }
}

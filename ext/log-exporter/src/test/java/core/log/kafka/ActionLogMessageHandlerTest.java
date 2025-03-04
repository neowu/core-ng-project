package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.Message;
import core.framework.util.Files;
import core.log.ActionLogMessageFactory;
import core.log.IntegrationTest;
import core.log.domain.ActionLogSchema;
import core.log.service.ArchiveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author neo
 */
class ActionLogMessageHandlerTest extends IntegrationTest {
    @Inject
    ActionLogSchema schema;
    private ActionLogMessageHandler handler;

    @BeforeEach
    void createActionLogMessageHandler() {
        handler = new ActionLogMessageHandler();
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
        var message = ActionLogMessageFactory.create();
        message.traceLog = "trace";

        handler.handle(List.of(new Message<>("key", message)));
        handler.handle(List.of(new Message<>("key", message)));
    }
}

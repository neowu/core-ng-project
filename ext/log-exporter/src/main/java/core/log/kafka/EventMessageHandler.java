package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.internal.json.JSONWriter;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.log.service.ArchiveService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author neo
 */
public class EventMessageHandler implements BulkMessageHandler<EventMessage> {
    private final JSONWriter<EventMessage> writer = new JSONWriter<>(EventMessage.class);

    @Inject
    ArchiveService archiveService;

    @Override
    public void handle(List<Message<EventMessage>> messages) throws IOException {
        LocalDate date = LocalDate.now();

        Path path = archiveService.initializeLogFilePath(archiveService.eventPath(date));
        try (BufferedOutputStream stream = new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND), 3 * 1024 * 1024)) {
            for (Message<EventMessage> message : messages) {
                stream.write(writer.toJSON(message.value));
                stream.write('\n');
            }
        }
    }
}

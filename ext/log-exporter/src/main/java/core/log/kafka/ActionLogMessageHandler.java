package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.ActionLogMessage;
import core.log.domain.ActionLogSchema;
import core.log.service.ArchiveService;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements BulkMessageHandler<ActionLogMessage> {
    @Inject
    ArchiveService archiveService;
    @Inject
    ActionLogSchema schema;

    @Override
    public void handle(List<Message<ActionLogMessage>> messages) throws IOException {
        LocalDate now = LocalDate.now();

        Path path = archiveService.localActionLogFilePath(now);
        archiveService.createParentDir(path);

        try (DataFileWriter<GenericData.Record> writer = new DataFileWriter<>(new SpecificDatumWriter<>(schema.schema))) {
            if (!Files.exists(path)) {
                writer.create(schema.schema, path.toFile());
            } else {
                writer.appendTo(path.toFile());
            }
            for (Message<ActionLogMessage> message : messages) {
                writer.append(schema.record(message.value));
            }
        }
    }
}

package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.internal.json.JSONWriter;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.ActionLogMessage;
import core.log.domain.ActionLogEntry;
import core.log.service.ArchiveService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author neo
 */
public class ActionLogMessageHandler implements BulkMessageHandler<ActionLogMessage> {
    private final JSONWriter<ActionLogEntry> writer = new JSONWriter<>(ActionLogEntry.class);

    @Inject
    ArchiveService archiveService;

    @Override
    public void handle(List<Message<ActionLogMessage>> messages) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        Path path = archiveService.initializeLogFilePath(archiveService.actionLogPath(now.toLocalDate()));
        try (BufferedOutputStream stream = new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND), 3 * 1024 * 1024)) {
            for (Message<ActionLogMessage> message : messages) {
                ActionLogEntry entry = entry(message.value);

                if (message.value.traceLog != null) {
                    entry.traceLogPath = archiveService.traceLogPath(now, entry.app, entry.id);
                    writeTraceLog(entry.traceLogPath, message.value.traceLog);
                }

                stream.write(writer.toJSON(entry));
                stream.write('\n');
            }
        }
    }

    private void writeTraceLog(String traceLogPath, String content) throws IOException {
        Path path = archiveService.initializeLogFilePath(traceLogPath);
        Files.writeString(path, content, StandardCharsets.UTF_8, CREATE, APPEND);
    }

    private ActionLogEntry entry(ActionLogMessage message) {
        var entry = new ActionLogEntry();
        entry.id = message.id;
        entry.date = message.date;
        entry.app = message.app;
        entry.host = message.host;
        entry.result = message.result;
        entry.action = message.action;
        entry.correlationIds = message.correlationIds;
        entry.clients = message.clients;
        entry.refIds = message.refIds;
        entry.errorCode = message.errorCode;
        entry.errorMessage = message.errorMessage;
        entry.elapsed = message.elapsed;
        entry.context = message.context;
        entry.stats = message.stats;
        entry.performanceStats = message.performanceStats;
        return entry;
    }
}

package core.log.service;

import core.framework.inject.Inject;
import core.framework.util.Files;
import core.log.ActionLogMessageFactory;
import core.log.IntegrationTest;
import core.log.domain.ActionLogSchema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ArchiveServiceTest extends IntegrationTest {
    @Inject
    ActionLogSchema schema;
    private ArchiveService archiveService;

    @BeforeEach
    void createArchiveService() {
        archiveService = new ArchiveService();
        archiveService.logDir = Files.tempDir();
    }

    @AfterEach
    void cleanup() {
        Files.deleteDir(archiveService.logDir);
    }

    @Test
    void remoteActionLogPath() {
        assertThat(archiveService.remoteActionLogPath(LocalDate.parse("2022-11-03")))
            .matches("/action/2022/action-2022-11-03-[a-z0-9]*.parquet");
    }

    @Test
    void remoteEventPath() {
        assertThat(archiveService.remoteEventPath(LocalDate.parse("2022-11-03")))
            .matches("/event/2022/event-2022-11-03-[a-z0-9]*.parquet");
    }

    @Test
    void localActionLogFilePath() {
        assertThat(archiveService.localActionLogFilePath(LocalDate.parse("2022-11-03")).toString())
            .matches(".*/action/2022/action-2022-11-03-[a-z0-9]*.avro");
    }

    @Test
    void localEventFilePath() {
        assertThat(archiveService.localEventFilePath(LocalDate.parse("2022-11-03")).toString())
            .matches(".*/event/2022/event-2022-11-03-[a-z0-9]*.avro");
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void cleanupArchive() {
        archiveService.cleanupArchive(LocalDate.now());
    }

    @Test
    void uploadActionLog() throws IOException {
        archiveService.uploadArchive(LocalDate.now());
    }

    @Test
    void convertToParquet() throws IOException {
        var message = ActionLogMessageFactory.create();

        Path avroPath = archiveService.localActionLogFilePath(LocalDate.parse("2022-11-07"));
        archiveService.createParentDir(avroPath);

        try (DataFileWriter<GenericData.Record> writer = new DataFileWriter<>(new SpecificDatumWriter<>(schema.schema))) {
            writer.create(schema.schema, avroPath.toFile());
            writer.append(schema.record(message));
            writer.append(schema.record(message));
        }

        Path parquetPath = archiveService.convertToParquet(avroPath);
        assertThat(parquetPath).exists();
    }
}

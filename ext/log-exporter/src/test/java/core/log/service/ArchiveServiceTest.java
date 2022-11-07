package core.log.service;

import core.framework.util.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ArchiveServiceTest {
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
    void actionLogPath() {
        assertThat(archiveService.actionLogPath(LocalDate.parse("2022-11-03")))
            .matches("/action/2022/action-2022-11-03-[a-z0-9]*.ndjson");
    }

    @Test
    void traceArchivePath() {
        assertThat(archiveService.traceLogPath(LocalDate.parse("2022-11-03"), "service", "id"))
            .isEqualTo("/trace/2022-11-03/service/id.txt");
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void cleanupArchive() {
        archiveService.deleteArchive(LocalDate.now());
    }

    @Test
    void uploadActionLog() {
        archiveService.uploadActionLog(LocalDate.now());
    }
}

package core.log.job;

import core.framework.scheduler.JobContext;
import core.log.service.ArchiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class ProcessLogJobTest {
    @Mock
    ArchiveService archiveService;
    private ProcessLogJob job;

    @BeforeEach
    void createProcessLogJob() {
        job = new ProcessLogJob();
        job.archiveService = archiveService;
    }

    @Test
    void execute() {
        job.execute(new JobContext("job", ZonedDateTime.parse("2022-11-07T01:00:00Z")));

        verify(archiveService).uploadArchive(LocalDate.parse("2022-11-06"));
        verify(archiveService).cleanupArchive(LocalDate.parse("2022-11-02"));
    }
}

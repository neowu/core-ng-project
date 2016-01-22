package core.log.job;

import core.log.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author neo
 */
public class CleanupOldIndexJobTest extends IntegrationTest {
    @Inject
    CleanupOldIndexJob job;

    @Test
    public void execute() throws Exception {
        job.execute();
    }
}
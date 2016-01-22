package core.log.job;

import core.log.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author neo
 */
public class DeleteOldIndexJobTest extends IntegrationTest {
    @Inject
    DeleteOldIndexJob job;

    @Test
    public void execute() throws Exception {
        job.execute();
    }
}
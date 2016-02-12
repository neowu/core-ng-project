package core.log.job;

import core.log.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    @Test
    public void createdDate() {
        assertEquals(LocalDate.of(2016, Month.FEBRUARY, 3), job.createdDate("action-2016-02-03").get());
        assertEquals(LocalDate.of(2015, Month.NOVEMBER, 15), job.createdDate("stat-2015-11-15").get());
        assertFalse(job.createdDate(".kibana").isPresent());
    }
}
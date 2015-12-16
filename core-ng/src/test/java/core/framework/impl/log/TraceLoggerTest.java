package core.framework.impl.log;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.assertThat;

/**
 * @author neo
 */
public class TraceLoggerTest {
    private TraceLogger logger;

    @Before
    public void createTraceLogger() {
        logger = new TraceLogger(null);
    }

    @Test
    public void traceLogFilePath() {
        String logFilePath = logger.traceLogFilePath("/log", LocalDateTime.of(2012, Month.OCTOBER, 2, 14, 5), "someController-method", "requestId");
        assertThat(logFilePath, containsString("/log/someController-method/201210021405.requestId."));
        assertThat(logFilePath, endsWith(".log"));
    }
}
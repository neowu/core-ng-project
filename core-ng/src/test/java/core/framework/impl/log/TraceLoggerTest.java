package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

/**
 * @author neo
 */
class TraceLoggerTest {
    private TraceLogger logger;

    @BeforeEach
    void createTraceLogger() {
        logger = new TraceLogger(null);
    }

    @Test
    void traceLogFilePath() {
        String logFilePath = logger.traceLogFilePath("/log", LocalDateTime.of(2012, Month.OCTOBER, 2, 14, 5), "someController-method", "requestId");
        assertThat(logFilePath, containsString("/log/someController-method/201210021405.requestId."));
        assertThat(logFilePath, endsWith(".log"));
    }
}

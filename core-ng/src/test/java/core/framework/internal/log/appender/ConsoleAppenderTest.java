package core.framework.internal.log.appender;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.internal.log.message.PerformanceStat;
import core.framework.internal.log.message.StatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ConsoleAppenderTest {
    private ConsoleAppender appender;

    @BeforeEach
    void createConsoleAppender() {
        appender = new ConsoleAppender();
    }

    @Test
    void filterLineSeparator() {
        assertThat(appender.filterLineSeparator("line1\nline2")).isEqualTo("line1 line2");
        assertThat(appender.filterLineSeparator("line1\r\nline2")).isEqualTo("line1  line2");
    }

    @Test
    void actionLogMessage() {
        var action = new ActionLogMessage();
        action.date = Instant.now();
        action.result = "OK";
        action.action = "action";
        action.correlationIds = List.of("refId1", "refId2");
        action.context = Map.of("context", "value");
        action.performanceStats = Map.of("db", perf(100, 1, 0), "redis", perf(120, 0, 1));
        action.clients = List.of("service");
        action.refIds = List.of("refId3");
        action.stats = Map.of("stat", 1.0);

        String message = appender.message(action);
        assertThat(message)
                .contains("| OK |")
                .contains("| correlationId=refId1,refId2 |")
                .contains("| action=action |")
                .contains("| context=value |")
                .contains("| client=service |")
                .contains("| refId=refId3 |")
                .contains("| stat=1.0 |")
                .contains("| dbCount=1 | dbReads=1 | dbWrites=0 | dbElapsed=100")
                .contains("| redisCount=1 | redisReads=0 | redisWrites=1 | redisElapsed=120");
    }

    @Test
    void statMessage() {
        var stat = new StatMessage();
        stat.date = Instant.now();
        stat.stats = Map.of("thread_count", 10.0, "cpu_usage", 0.01);

        String message = appender.message(stat);
        assertThat(message)
                .contains("| thread_count=10.000000000")
                .contains("| cpu_usage=0.010000000");
    }

    private PerformanceStat perf(long elapsed, int read, int write) {
        PerformanceStat stat = new PerformanceStat();
        stat.count = 1;
        stat.totalElapsed = elapsed;
        stat.readEntries = read;
        stat.writeEntries = write;
        return stat;
    }
}

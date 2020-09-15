package core.framework.internal.log.appender;

import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.log.message.StatMessage;
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
        action.context = Map.of("context", List.of("value"));
        action.performanceStats = Map.of("db", perf(100, 1, 0), "redis", perf(120, 0, 1));
        action.clients = List.of("service");
        action.refIds = List.of("refId3");
        action.stats = Map.of("cpu_time", 100.0);
        action.elapsed = 100L;

        String message = appender.message(action);
        assertThat(message)
                .contains("| OK |")
                .contains("| elapsed=100 |")
                .contains("| correlation_id=refId1,refId2 |")
                .contains("| action=action |")
                .contains("| context=value |")
                .contains("| client=service |")
                .contains("| ref_id=refId3 |")
                .contains("| cpu_time=100 |")
                .contains("| db_count=1 | db_reads=1 | db_writes=0 | db_elapsed=100")
                .contains("| redis_count=1 | redis_reads=0 | redis_writes=1 | redis_elapsed=120");
    }

    @Test
    void statMessage() {
        var stat = new StatMessage();
        stat.date = Instant.now();
        stat.stats = Map.of("thread_count", 10.0, "cpu_usage", 0.01);
        stat.info = Map.of("info", "text");

        String message = appender.message(stat);
        assertThat(message)
                .contains("| thread_count=10")
                .contains("| cpu_usage=0.01")
                .contains("| info=text");
    }

    private PerformanceStatMessage perf(long elapsed, int read, int write) {
        PerformanceStatMessage stat = new PerformanceStatMessage();
        stat.count = 1;
        stat.totalElapsed = elapsed;
        stat.readEntries = read;
        stat.writeEntries = write;
        return stat;
    }
}

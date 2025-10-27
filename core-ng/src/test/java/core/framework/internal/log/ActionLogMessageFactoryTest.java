package core.framework.internal.log;

import core.framework.log.Markers;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionLogMessageFactoryTest {
    private ActionLogMessageFactory factory;

    @BeforeEach
    void createActionLogMessageFactory() {
        factory = new ActionLogMessageFactory();
    }

    @Test
    void actionLog() {
        var log = new ActionLog("begin", null);
        log.action("action");
        log.process(new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message", null, null));
        log.track("db", 1000, 1, 2);
        log.track("http", 2000, 0, 0);
        log.context("order_id", "id1", "id2");

        ActionLogMessage message = factory.create(log);

        assertThat(message).isNotNull();
        assertThat(message.app).isEqualTo(LogManager.APP_NAME);
        assertThat(message.action).isEqualTo("action");
        assertThat(message.errorCode).isEqualTo("ERROR_CODE");
        assertThat(message.traceLog).isNotEmpty();

        PerformanceStatMessage stats = message.performanceStats.get("db");
        assertThat(stats.totalElapsed).isEqualTo(1000);
        assertThat(stats.count).isEqualTo(1);
        assertThat(stats.readEntries).isEqualTo(1);
        assertThat(stats.writeEntries).isEqualTo(2);

        stats = message.performanceStats.get("http");
        assertThat(stats.totalElapsed).isEqualTo(2000);
        assertThat(stats.count).isEqualTo(1);
        assertThat(stats.readEntries).isNull();
        assertThat(stats.writeEntries).isNull();
    }
}

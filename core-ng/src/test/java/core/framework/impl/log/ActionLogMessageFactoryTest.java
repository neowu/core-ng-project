package core.framework.impl.log;

import core.framework.internal.log.message.ActionLogMessage;
import core.framework.internal.log.message.PerformanceStat;
import core.framework.log.Markers;
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
        var log = new ActionLog("begin");
        log.action("action");
        log.process(new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message", null, null));
        log.track("db", 1000, 1, 2);

        ActionLogMessage message = factory.create(log);

        assertThat(message).isNotNull();
        assertThat(message.app).isEqualTo(LogManager.APP_NAME);
        assertThat(message.action).isEqualTo("action");
        assertThat(message.errorCode).isEqualTo("ERROR_CODE");
        assertThat(message.traceLog).isNotEmpty();

        PerformanceStat statMessage = message.performanceStats.get("db");
        assertThat(statMessage.totalElapsed).isEqualTo(1000);
        assertThat(statMessage.count).isEqualTo(1);
        assertThat(statMessage.readEntries).isEqualTo(1);
        assertThat(statMessage.writeEntries).isEqualTo(2);
    }

    @Test
    void trace() {
        var log = new ActionLog("begin");
        String trace = factory.trace(log, 200);
        String suffix = "...(truncated)";
        assertThat(trace).hasSize(200 + suffix.length())
                         .contains("ActionLog - begin")
                         .endsWith(suffix);
    }
}

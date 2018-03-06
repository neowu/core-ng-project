package core.framework.impl.log;

import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.log.Markers;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
class KafkaAppenderTest {
    private KafkaAppender kafkaAppender;

    @BeforeEach
    void createLogForwarder() {
        kafkaAppender = new KafkaAppender("url", "app", new MockProducer<>());
    }

    @Test
    void forwardActionLog() {
        ActionLog log = new ActionLog("begin");
        log.action("action");
        log.process(new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message", null, null));
        log.track("db", 1000, 1, 2);

        kafkaAppender.forwardActionLog(log);
        ActionLogMessage message = (ActionLogMessage) kafkaAppender.queue.poll();

        assertEquals("app", message.app);
        assertEquals("action", message.action);
        assertEquals("ERROR_CODE", message.errorCode);
        assertFalse(Strings.isEmpty(message.traceLog));
        PerformanceStatMessage statMessage = message.performanceStats.get("db");
        assertEquals(1000, (long) statMessage.totalElapsed);
        assertEquals(1, statMessage.count.intValue());
        assertEquals(1, statMessage.readEntries.intValue());
        assertEquals(2, statMessage.writeEntries.intValue());
    }
}

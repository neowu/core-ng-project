package core.framework.impl.log;

import core.framework.log.Markers;
import core.framework.log.message.ActionLogMessage;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author neo
 */
class LogForwarderTest {
    private LogForwarder logForwarder;

    @BeforeEach
    void createLogForwarder() {
        logForwarder = new LogForwarder("url", "app", new MockProducer<>());
    }

    @Test
    void forwardLog() {
        ActionLog log = new ActionLog("begin");
        log.action("action");
        log.process(new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message", null, null));

        logForwarder.forwardLog(log);
        ActionLogMessage message = (ActionLogMessage) logForwarder.queue.poll();

        assertEquals("app", message.app);
        assertEquals("action", message.action);
        assertEquals("ERROR_CODE", message.errorCode);
        assertFalse(Strings.isEmpty(message.traceLog));
    }
}

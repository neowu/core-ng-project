package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.log.Markers;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaAppenderTest {
    private KafkaAppender kafkaAppender;
    private LogFilter filter;

    @BeforeEach
    void createLogForwarder() {
        filter = new LogFilter();
        kafkaAppender = new KafkaAppender("url", "app", new MockProducer<>());
    }

    @Test
    void forward() {
        var log = new ActionLog("begin");
        log.action("action");
        log.process(new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message", null, null));
        log.track("db", 1000, 1, 2);

        kafkaAppender.forward(log, filter);
        ActionLogMessage message = (ActionLogMessage) kafkaAppender.queue.poll();

        assertThat(message).isNotNull();
        assertThat(message.app).isEqualTo("app");
        assertThat(message.action).isEqualTo("action");
        assertThat(message.errorCode).isEqualTo("ERROR_CODE");
        assertThat(message.traceLog).isNotEmpty();
        PerformanceStatMessage statMessage = message.performanceStats.get("db");
        assertThat(statMessage.totalElapsed).isEqualTo(1000);
        assertThat(statMessage.count).isEqualTo(1);
        assertThat(statMessage.readEntries).isEqualTo(1);
        assertThat(statMessage.writeEntries).isEqualTo(2);
    }
}

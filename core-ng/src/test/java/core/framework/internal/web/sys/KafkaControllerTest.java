package core.framework.internal.web.sys;

import core.framework.internal.kafka.KafkaMessage;
import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaControllerTest {
    private KafkaController controller;
    private LogManager logManager;
    private ActionLog actionLog;

    @BeforeEach
    void createKafkaController() {
        controller = new KafkaController();
        logManager = new LogManager();
        actionLog = logManager.begin("begin", null);
    }

    @AfterEach
    void cleanup() {
        logManager.end("end");
    }

    @Test
    void record() {
        ProducerRecord<byte[], byte[]> record = controller.record("topic", "key", new byte[0], actionLog);
        assertThat(record.headers().lastHeader(KafkaMessage.HEADER_TRACE).value()).asString().isEqualTo(Trace.CASCADE.name());
    }
}

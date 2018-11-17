package core.framework.impl.web.management;

import core.framework.impl.log.LogManager;
import core.framework.internal.kafka.MessageHeaders;
import core.framework.util.Strings;
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

    @BeforeEach
    void createKafkaController() {
        controller = new KafkaController(null);
        logManager = new LogManager();
        logManager.begin("begin");
    }

    @AfterEach
    void cleanup() {
        logManager.end("end");
    }

    @Test
    void record() {
        ProducerRecord<byte[], byte[]> record = controller.record("topic", "key", new byte[0]);
        assertThat(record.headers().lastHeader(MessageHeaders.HEADER_TRACE).value()).isEqualTo(Strings.bytes("true"));
    }
}

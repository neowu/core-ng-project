package core.framework.internal.web.sys;

import core.framework.internal.kafka.KafkaMessage;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import org.apache.kafka.clients.producer.ProducerRecord;
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
        controller = new KafkaController();
        logManager = new LogManager();
    }

    @Test
    void record() {
        logManager.run("test", null, actionLog -> {
            ProducerRecord<byte[], byte[]> record = controller.record("topic", "key", new byte[0], actionLog);
            assertThat(record.headers().lastHeader(KafkaMessage.HEADER_TRACE).value()).asString().isEqualTo(Trace.CASCADE.name());
            return null;
        });
    }
}

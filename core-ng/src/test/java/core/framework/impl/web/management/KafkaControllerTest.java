package core.framework.impl.web.management;

import core.framework.impl.kafka.MessageHeaders;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class KafkaControllerTest {
    private KafkaController controller;

    @BeforeEach
    void createKafkaController() {
        controller = new KafkaController(null);
    }

    @Test
    void record() {
        ProducerRecord<String, byte[]> record = controller.record("topic", "key", new byte[0]);
        assertThat(record.headers().lastHeader(MessageHeaders.HEADER_TRACE).value()).isEqualTo(Strings.bytes("true"));
    }
}

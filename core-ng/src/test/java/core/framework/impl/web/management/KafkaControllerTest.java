package core.framework.impl.web.management;

import core.framework.impl.kafka.MessageHeaders;
import core.framework.impl.log.ActionLog;
import core.framework.impl.log.LogManager;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class KafkaControllerTest {
    private KafkaController controller;
    private LogManager logManager;

    @BeforeEach
    void createKafkaController() {
        logManager = mock(LogManager.class);
        controller = new KafkaController(null, logManager);
    }

    @Test
    void record() {
        var actionLog = new ActionLog(null);
        when(logManager.currentActionLog()).thenReturn(actionLog);
        ProducerRecord<String, byte[]> record = controller.record("topic", "key", new byte[0]);
        assertThat(record.headers().lastHeader(MessageHeaders.HEADER_TRACE).value()).isEqualTo(Strings.bytes("true"));
    }
}

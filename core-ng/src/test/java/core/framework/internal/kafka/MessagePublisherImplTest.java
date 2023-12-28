package core.framework.internal.kafka;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import core.framework.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class MessagePublisherImplTest {
    @Mock
    MessageProducer producer;
    private MessagePublisherImpl<TestMessage> publisher;
    private LogManager logManager;

    @BeforeEach
    void createMessagePublisher() {
        publisher = new MessagePublisherImpl<>(producer, "topic", TestMessage.class);
        logManager = new LogManager();
    }

    @Test
    void publish() {
        ActionLog actionLog = logManager.begin("begin", null);
        actionLog.correlationIds = List.of("correlationId");
        actionLog.trace = Trace.CASCADE;

        var message = new TestMessage();
        message.stringField = "value";

        publisher.publish(message);
        verify(producer).send(argThat(record -> {
            assertThat(record.key()).isNull();
            assertThat(new String(record.headers().lastHeader(KafkaMessage.HEADER_CORRELATION_ID).value(), UTF_8)).isEqualTo("correlationId");
            assertThat(new String(record.headers().lastHeader(KafkaMessage.HEADER_REF_ID).value(), UTF_8)).isEqualTo(actionLog.id);
            assertThat(new String(record.headers().lastHeader(KafkaMessage.HEADER_TRACE).value(), UTF_8)).isEqualTo(Trace.CASCADE.name());
            return true;
        }));

        publisher.publish("key", message);
        verify(producer).send(argThat(record -> Arrays.equals(Strings.bytes("key"), record.key()) && "topic".equals(record.topic())));

        logManager.end("end");
    }
}

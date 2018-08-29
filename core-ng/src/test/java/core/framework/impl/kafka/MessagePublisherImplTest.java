package core.framework.impl.kafka;

import core.framework.impl.log.LogManager;
import core.framework.kafka.MessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * @author neo
 */
class MessagePublisherImplTest {
    private MessagePublisher<TestMessage> publisher;
    private MessageProducer producer;
    private LogManager logManager;

    @BeforeEach
    void createMessagePublisher() {
        producer = Mockito.mock(MessageProducer.class);
        logManager = new LogManager();
        publisher = new MessagePublisherImpl<>(producer, "topic", TestMessage.class, logManager);
    }

    @Test
    void publish() {
        logManager.begin("begin");
        logManager.currentActionLog().refId("ref-id");
        var message = new TestMessage();
        message.stringField = "value";
        publisher.publish(message);
        logManager.end("end");

        verify(producer).send(argThat(record -> {
            assertThat(record.key()).hasSize(36);
            assertThat(record.headers().lastHeader(MessageHeaders.HEADER_CLIENT_IP).value()).isNotNull();
            assertThat(new String(record.headers().lastHeader(MessageHeaders.HEADER_REF_ID).value(), UTF_8)).isEqualTo("ref-id");
            return true;
        }));
    }
}

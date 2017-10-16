package core.framework.impl.kafka;

import core.framework.impl.log.LogManager;
import core.framework.kafka.MessagePublisher;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author neo
 */
class KafkaMessagePublisherTest {
    private MessagePublisher<TestMessage> messagePublisher;
    private MockProducer<String, byte[]> producer;
    private LogManager logManager;

    @BeforeEach
    void createMessagePublisher() {
        producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        logManager = new LogManager();
        messagePublisher = new KafkaMessagePublisher<>(producer, "topic", TestMessage.class, logManager);
    }

    @Test
    void publish() {
        logManager.begin("begin");
        logManager.currentActionLog().refId("ref-id");
        TestMessage message = new TestMessage();
        message.stringField = "value";
        messagePublisher.publish(message);
        logManager.end("end");

        assertEquals(1, producer.history().size());
        ProducerRecord<String, byte[]> record = producer.history().get(0);
        assertEquals(36, record.key().length());
        assertNotNull(record.headers().lastHeader(KafkaHeaders.HEADER_CLIENT_IP).value());
        assertArrayEquals(Strings.bytes("ref-id"), record.headers().lastHeader(KafkaHeaders.HEADER_REF_ID).value());
    }
}

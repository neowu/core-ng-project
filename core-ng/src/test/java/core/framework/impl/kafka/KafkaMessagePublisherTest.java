package core.framework.impl.kafka;

import core.framework.api.kafka.MessagePublisher;
import core.framework.api.util.Strings;
import core.framework.impl.log.LogManager;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author neo
 */
public class KafkaMessagePublisherTest {
    MessagePublisher<TestMessage> messagePublisher;
    MockProducer<String, byte[]> producer;
    LogManager logManager;

    @Before
    public void createMessagePublisher() {
        producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        logManager = new LogManager();
        messagePublisher = new KafkaMessagePublisher<>(producer, "topic", TestMessage.class, logManager);
    }

    @Test
    public void publish() {
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

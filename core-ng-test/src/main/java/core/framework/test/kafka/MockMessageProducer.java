package core.framework.test.kafka;

import core.framework.impl.kafka.MessageProducer;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

/**
 * @author neo
 */
public class MockMessageProducer implements MessageProducer {
    private final Producer<byte[], byte[]> producer;

    public MockMessageProducer() {
        var serializer = new ByteArraySerializer();
        producer = new MockProducer<>(true, serializer, serializer);
    }

    @Override
    public void send(ProducerRecord<byte[], byte[]> record) {
        producer.send(record);
    }
}

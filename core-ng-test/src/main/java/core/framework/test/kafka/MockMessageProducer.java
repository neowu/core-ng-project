package core.framework.test.kafka;

import core.framework.impl.kafka.MessageProducer;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author neo
 */
public class MockMessageProducer implements MessageProducer {
    private final Producer<String, byte[]> producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());

    @Override
    public void send(ProducerRecord<String, byte[]> record) {
        producer.send(record);
    }
}

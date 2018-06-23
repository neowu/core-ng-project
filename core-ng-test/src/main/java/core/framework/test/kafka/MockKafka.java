package core.framework.test.kafka;

import core.framework.impl.kafka.Kafka;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * @author neo
 */
public class MockKafka extends Kafka {
    public MockKafka() {
        super(null);
    }

    @Override
    protected Producer<String, byte[]> createProducer() {
        return new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
    }
}

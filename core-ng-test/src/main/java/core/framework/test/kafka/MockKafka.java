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
    @Override
    public Producer<String, byte[]> producer() {
        return new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
    }
}

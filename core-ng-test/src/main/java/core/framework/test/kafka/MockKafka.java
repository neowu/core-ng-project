package core.framework.test.kafka;

import core.framework.impl.kafka.Kafka;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Set;

/**
 * @author neo
 */
public class MockKafka extends Kafka {
    public MockKafka() {
        super(null, null);
    }

    @Override
    public Producer<String, byte[]> producer() {
        return new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
    }

    @Override
    public Consumer<String, byte[]> consumer(String group, Set<String> topics) {
        MockConsumer<String, byte[]> consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        consumer.subscribe(topics);
        return consumer;
    }
}

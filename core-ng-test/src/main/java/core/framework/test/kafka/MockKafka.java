package core.framework.test.kafka;

import core.framework.impl.kafka.Kafka;
import core.framework.util.Lists;
import core.framework.util.Maps;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class MockKafka extends Kafka {
    public MockKafka() {
        super(null, null);
    }

    @Override
    protected Producer<String, byte[]> createProducer() {
        return new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
    }

    @Override
    public Consumer<String, byte[]> consumer(String group, Set<String> topics) {
        MockConsumer<String, byte[]> consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

        List<TopicPartition> assignments = Lists.newArrayList();
        Map<TopicPartition, Long> offsets = Maps.newHashMap();
        for (String topic : topics) {
            assignments.add(new TopicPartition(topic, 0));
            offsets.put(new TopicPartition("topic", 0), 0L);
        }
        consumer.assign(assignments);
        consumer.updateBeginningOffsets(offsets);

        return consumer;
    }
}

package core.framework.test.kafka;

import core.framework.util.Sets;
import core.framework.util.Strings;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class MockKafkaTest {
    private MockKafka kafka;

    @BeforeEach
    void createMockKafka() {
        kafka = new MockKafka();
    }

    @Test
    void consumer() {
        MockConsumer<String, byte[]> consumer = (MockConsumer<String, byte[]>) kafka.consumer("local", Sets.newHashSet("topic"));
        consumer.addRecord(new ConsumerRecord<>("topic", 0, 0, "key", Strings.bytes("value")));
        ConsumerRecords<String, byte[]> records = consumer.poll(0);
        assertEquals(1, records.count());
        ConsumerRecord<String, byte[]> record = records.iterator().next();
        assertEquals("key", record.key());
    }
}

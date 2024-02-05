package core.framework.internal.kafka;

import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.MessageHandler;
import core.framework.util.Strings;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageListenerThreadProcessTest {
    @Mock
    MessageHandler<TestMessage> messageHandler;
    @Mock
    BulkMessageHandler<TestMessage> bulkMessageHandler;
    @Mock
    Consumer<String, byte[]> consumer;
    private MessageListenerThread thread;

    @BeforeEach
    void createMessageListenerThread() {
        var listener = new MessageListener(null, null, null, 300_000L);
        listener.processes.put("topic1", new MessageProcess<>(messageHandler, TestMessage.class));
        listener.bulkProcesses.put("topic2", new MessageProcess<>(bulkMessageHandler, TestMessage.class));
        thread = new MessageListenerThread("kafka-listener", consumer, listener);
    }

    @Test
    void process() throws InterruptedException {
        Map<TopicPartition, List<ConsumerRecord<String, byte[]>>> entries = new HashMap<>();
        entries.put(topic("topic1"), List.of(record("topic1", "key1"),
            record("topic1", "key2"),
            record("topic1", "key2"),
            record("topic1", null)));
        entries.put(topic("topic2"), List.of(record("topic2", "key1")));
        when(consumer.poll(any())).thenReturn(new ConsumerRecords<>(entries));

        List<KafkaMessages> messages = new ArrayList<>(thread.poll());
        assertThat(messages.get(0).topic).isEqualTo("topic1");
        assertThat(messages.get(0).ordered.get("key2").subsequent).hasSize(1);

        thread.processAll(messages);
        thread.shutdown();
        thread.awaitTermination(1000);
    }

    ConsumerRecord<String, byte[]> record(String topic, String key) {
        return new ConsumerRecord<>(topic, 1, 1, key, Strings.bytes("{}"));
    }

    TopicPartition topic(String topic) {
        return new TopicPartition(topic, 1);
    }
}

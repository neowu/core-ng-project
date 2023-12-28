package core.framework.internal.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KafkaMessages {
    final Map<String, KafkaMessage> ordered = new HashMap<>();
    final List<KafkaMessage> unordered = new ArrayList<>();
    final String topic;

    boolean bulk;
    int count;
    int size;

    KafkaMessages(String topic) {
        this.topic = topic;
    }

    void addOrdered(ConsumerRecord<String, byte[]> record) {
        var message = new KafkaMessage(record);
        if (message.key != null) {
            KafkaMessage root = ordered.get(message.key);
            if (root != null) root.addSubsequent(message);
            else ordered.put(message.key, message);
        } else {
            unordered.add(message);
        }
        count++;
        size += message.value.length;
    }

    void addUnordered(ConsumerRecord<String, byte[]> record) {
        var message = new KafkaMessage(record);
        unordered.add(message);
        count++;
        size += message.value.length;
    }
}

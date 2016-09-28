package core.framework.impl.kafka;

import core.framework.api.kafka.MessageProducer;
import core.framework.impl.json.JSONWriter;

/**
 * @author neo
 */
public class KafkaMessageProducer<T> implements MessageProducer<T> {
    private final Kafka kafka;
    private final JSONWriter<T> writer;
    private final String topic;

    public KafkaMessageProducer(Kafka kafka, Class<T> messageClass, String topic) {
        this.kafka = kafka;
        writer = JSONWriter.of(messageClass);
        this.topic = topic;
    }

    @Override
    public void send(String key, T message) {
        send(topic, key, message);
    }

    @Override
    public void send(String topic, String key, T message) {
        kafka.send(topic, key, writer.toJSON(message));
    }
}

package core.framework.impl.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * @author neo
 */
public class Kafka implements AutoCloseable {
    public String host;
    Producer<String, byte[]> producer;

    Producer<String, byte[]> producer() {
        if (producer == null) {
            producer = createProducer();
        }
        return producer;
    }

    private Producer<String, byte[]> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        props.put(ProducerConfig.ACKS_CONFIG, "-1");
        return new org.apache.kafka.clients.producer.KafkaProducer<>(props, new StringSerializer(), new ByteArraySerializer());
    }

    public void send(String topic, String key, byte[] value) {
        producer().send(new ProducerRecord<>(topic, key, value));
    }

    @Override
    public void close() throws Exception {
        if (producer != null)
            producer.close();
    }
}

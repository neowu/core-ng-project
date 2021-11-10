package core.framework.internal.kafka;

import core.framework.kafka.KafkaException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MessageProducerTest {
    private MessageProducer producer;

    @BeforeEach
    void createMessageProducer() {
        producer = new MessageProducer(null, null, 1024);
    }

    @Test
    void createProducer() {
        Producer<byte[], byte[]> producer = this.producer.createProducer(new KafkaURI("localhost"));
        assertThat(producer).isNotNull();
        producer.close(Duration.ZERO);
    }

    @Test
    void close() {
        producer.close(-1);
    }

    @Test
    void onCompletion() {
        var callback = new MessageProducer.KafkaCallback(new ProducerRecord<>("topic", new byte[0]));
        callback.onCompletion(null, new KafkaException("unexpected"));
    }
}

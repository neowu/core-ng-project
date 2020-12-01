package core.framework.internal.kafka;

import core.framework.kafka.KafkaException;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class MessageProducerTest {
    @Mock
    KafkaURI uri;
    private MessageProducer producer;

    @BeforeEach
    void createMessageProducer() {
        producer = new MessageProducer(uri, null, 1024);
    }

    @Test
    void send() {
        var record = new ProducerRecord<byte[], byte[]>("topic", Strings.bytes("{}"));
        when(uri.resolveURI()).thenReturn(Boolean.FALSE);
        assertThatThrownBy(() -> producer.send(record))
                .isInstanceOf(KafkaException.class)
                .hasMessageContaining("kafka uri is not resolvable");
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

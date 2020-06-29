package core.framework.internal.kafka;

import core.framework.kafka.KafkaException;
import core.framework.util.Strings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class MessageProducerTest {
    private MessageProducer producer;
    private KafkaURI uri;

    @BeforeEach
    void createMessageProducer() {
        uri = mock(KafkaURI.class);
        producer = new MessageProducer(uri, null);
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
    void close() {
        producer.close(-1);
    }
}

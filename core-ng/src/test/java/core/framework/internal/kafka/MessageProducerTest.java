package core.framework.internal.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class MessageProducerTest {
    private MessageProducer producer;

    @BeforeEach
    void createMessageProducerImpl() {
        producer = new MessageProducer("localhost:9092", null);
    }

    @Test
    void close() {
        producer.close(-1);
    }
}

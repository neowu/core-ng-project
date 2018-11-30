package core.framework.internal.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author neo
 */
class MessageProducerImplTest {
    private MessageProducerImpl producer;

    @BeforeEach
    void createMessageProducerImpl() {
        producer = new MessageProducerImpl("localhost:9092", null);
    }

    @Test
    void close() {
        producer.close(-1);
    }
}

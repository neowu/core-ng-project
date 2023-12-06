package core.framework.internal.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MessageListenerTest {
    private MessageListener listener;

    @BeforeEach
    void createMessageListener() {
        listener = new MessageListener(new KafkaURI("localhost"), null, null, 300_000L);
    }

    @Test
    void threadName() {
        assertThat(listener.threadName(null)).isEqualTo("kafka-listener");
        assertThat(listener.threadName("name")).isEqualTo("kafka-listener-name");
    }

    @Test
    void createConsumer() {
        assertThat(listener.createConsumer()).isNotNull();
    }
}

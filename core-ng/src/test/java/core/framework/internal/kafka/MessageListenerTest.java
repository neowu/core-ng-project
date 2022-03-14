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
    void listenerThreadName() {
        assertThat(listener.listenerThreadName(null, 0)).isEqualTo("kafka-listener-0");
        assertThat(listener.listenerThreadName("name", 2)).isEqualTo("kafka-listener-name-2");
    }

    @Test
    void createConsumer() {
        assertThat(listener.createConsumer()).isNotNull();
    }
}

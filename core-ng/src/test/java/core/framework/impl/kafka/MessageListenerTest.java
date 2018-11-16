package core.framework.impl.kafka;

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
        listener = new MessageListener(null, null, null);
    }

    @Test
    void clientId() {
        assertThat(listener.clientId("test-service", null, 0)).isEqualTo("test-service-0");
        assertThat(listener.clientId("test-service", "name", 1)).isEqualTo("test-service-name-1");
    }

    @Test
    void listenerThreadName() {
        assertThat(listener.listenerThreadName(null, 0)).isEqualTo("kafka-listener-0");
        assertThat(listener.listenerThreadName("name", 2)).isEqualTo("kafka-listener-name-2");
    }
}

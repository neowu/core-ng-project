package core.framework.impl.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class KafkaMessageListenerThreadTest {
    private KafkaMessageListenerThread thread;

    @BeforeEach
    void createKafkaMessageListenerThread() {
        thread = new KafkaMessageListenerThread("listener-thread-1", null, new KafkaMessageListener(null, null, null));
    }

    @Test
    void longProcessThreshold() {
        assertEquals(5, thread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 100), 0.000001);

        assertEquals(500, thread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 1), 0.000001);
    }
}

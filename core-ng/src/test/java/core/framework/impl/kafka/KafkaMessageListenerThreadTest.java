package core.framework.impl.kafka;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class KafkaMessageListenerThreadTest {
    @Test
    void longProcessThreshold() {
        assertEquals(5, KafkaMessageListenerThread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 100), 0.000001);

        assertEquals(500, KafkaMessageListenerThread.longProcessThreshold(Duration.ofNanos(500).toNanos(), 1, 1), 0.000001);
    }
}

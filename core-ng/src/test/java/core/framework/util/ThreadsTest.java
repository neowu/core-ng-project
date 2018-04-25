package core.framework.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ThreadsTest {
    @Test
    void sleepTime() {
        long sleepTime = Threads.sleepTime(Duration.ofMillis(10));
        assertTrue(sleepTime >= 8 && sleepTime <= 12);

        sleepTime = Threads.sleepTime(Duration.ofMillis(100));
        assertTrue(sleepTime >= 80 && sleepTime <= 120);

        sleepTime = Threads.sleepTime(Duration.ofSeconds(10));
        assertTrue(sleepTime >= 8000 && sleepTime <= 12000);
    }
}

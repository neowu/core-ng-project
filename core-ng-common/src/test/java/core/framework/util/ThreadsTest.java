package core.framework.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ThreadsTest {
    @Test
    void sleepTime() {
        long sleepTime = Threads.sleepTime(Duration.ofMillis(10));
        assertThat(sleepTime).isGreaterThanOrEqualTo(8).isLessThanOrEqualTo(12);

        sleepTime = Threads.sleepTime(Duration.ofMillis(100));
        assertThat(sleepTime).isGreaterThanOrEqualTo(80).isLessThanOrEqualTo(120);

        sleepTime = Threads.sleepTime(Duration.ofSeconds(10));
        assertThat(sleepTime).isGreaterThanOrEqualTo(8000).isLessThanOrEqualTo(12000);
    }

    @Test
    void sleepRoughly() {
        Threads.sleepRoughly(Duration.ZERO);
    }
}

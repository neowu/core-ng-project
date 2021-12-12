package app.monitor.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class GCStatTest {
    private GCStat stat;

    @BeforeEach
    void createGCStat() {
        stat = new GCStat("test");
    }

    @Test
    void elapsed() {
        assertThat(stat.elapsed(0)).isEqualTo(Duration.ZERO.toNanos());
        assertThat(stat.elapsed(500)).isEqualTo(Duration.ofMillis(500).toNanos());
        assertThat(stat.elapsed(2000)).isEqualTo(Duration.ofMillis(1500).toNanos());

        // target reset
        assertThat(stat.elapsed(300)).isEqualTo(Duration.ofMillis(0).toNanos());
        assertThat(stat.elapsed(500)).isEqualTo(Duration.ofMillis(200).toNanos());
    }

    @Test
    void count() {
        assertThat(stat.count(0)).isEqualTo(0);
        assertThat(stat.count(1)).isEqualTo(1);
        assertThat(stat.count(2)).isEqualTo(1);

        assertThat(stat.count(0)).isEqualTo(0);
        assertThat(stat.count(2)).isEqualTo(2);
    }
}

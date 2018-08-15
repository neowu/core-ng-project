package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PerformanceStatTest {
    private PerformanceStat stat;

    @BeforeEach
    void createPerformanceStat() {
        stat = new PerformanceStat();
    }

    @Test
    void track() {
        stat.track(100, null, null);
        assertThat(stat.count).isEqualTo(1);
        assertThat(stat.readEntries).isNull();
        assertThat(stat.writeEntries).isNull();

        stat.track(100, 1, 0);
        assertThat(stat.count).isEqualTo(2);
        assertThat(stat.elapsedTime).isEqualTo(200);
        assertThat(stat.readEntries).isEqualTo(1);
        assertThat(stat.writeEntries).isEqualTo(0);

        stat.track(100, 1, 2);
        assertThat(stat.count).isEqualTo(3);
        assertThat(stat.elapsedTime).isEqualTo(300);
        assertThat(stat.readEntries).isEqualTo(2);
        assertThat(stat.writeEntries).isEqualTo(2);
    }
}

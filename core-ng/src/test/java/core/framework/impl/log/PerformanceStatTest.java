package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PerformanceStatTest {
    private PerformanceStat performanceStat;

    @BeforeEach
    void createPerformanceStat() {
        performanceStat = new PerformanceStat();
    }

    @Test
    void increaseReadEntries() {
        performanceStat.increaseReadEntries(null);
        assertThat(performanceStat.readEntries).isNull();

        performanceStat.increaseReadEntries(1);
        assertThat(performanceStat.readEntries).isEqualTo(1);

        performanceStat.increaseReadEntries(2);
        assertThat(performanceStat.readEntries).isEqualTo(3);
    }

    @Test
    void increaseWriteEntries() {
        performanceStat.increaseWriteEntries(null);
        assertThat(performanceStat.writeEntries).isNull();

        performanceStat.increaseWriteEntries(1);
        assertThat(performanceStat.writeEntries).isEqualTo(1);

        performanceStat.increaseWriteEntries(2);
        assertThat(performanceStat.writeEntries).isEqualTo(3);
    }
}

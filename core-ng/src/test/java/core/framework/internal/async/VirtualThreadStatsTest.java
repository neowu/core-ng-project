package core.framework.internal.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualThreadStatsTest {
    private VirtualThread.Stats stats;

    @BeforeEach
    void createStats() {
        stats = new VirtualThread.Stats();
    }

    @Test
    void maxCount() {
        stats.increase();
        stats.increase();
        stats.decrease();
        assertThat(stats.maxCount()).isEqualTo(2);
        assertThat(stats.maxCount()).isEqualTo(1);
    }
}

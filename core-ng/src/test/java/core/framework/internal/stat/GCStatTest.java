package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.management.GarbageCollectorMXBean;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
@ExtendWith(MockitoExtension.class)
class GCStatTest {
    @Mock
    GarbageCollectorMXBean bean;
    private GCStat stat;

    @BeforeEach
    void createGCStat() {
        when(bean.getName()).thenReturn("G1 Young Generation");
        stat = GCStat.of(bean);
    }

    @Test
    void collector() {
        assertThat(GCStat.collector("G1 Young Generation")).isEqualTo("young");
        assertThat(GCStat.collector("G1 Old Generation")).isEqualTo("old");
        assertThat(GCStat.collector("PS Scavenge")).isEqualTo("young");
        assertThat(GCStat.collector("PS MarkSweep")).isEqualTo("old");
        assertThat(GCStat.collector("Copy")).isNull();
    }

    @Test
    void elapsed() {
        when(bean.getCollectionTime()).thenReturn(500L, 1000L, 3000L);
        assertThat(stat.elapsed()).isEqualTo(Duration.ZERO.toNanos());
        assertThat(stat.elapsed()).isEqualTo(Duration.ofMillis(500).toNanos());
        assertThat(stat.elapsed()).isEqualTo(Duration.ofMillis(2000).toNanos());
    }

    @Test
    void count() {
        when(bean.getCollectionCount()).thenReturn(1L, 2L, 3L);
        assertThat(stat.count()).isEqualTo(0);
        assertThat(stat.count()).isEqualTo(1);
        assertThat(stat.count()).isEqualTo(1);
    }
}

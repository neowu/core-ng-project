package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class GCStatTest {
    private GCStat stat;
    private GarbageCollectorMXBean bean;

    @BeforeEach
    void createGCStat() {
        bean = mock(GarbageCollectorMXBean.class);
        when(bean.getName()).thenReturn("G1 Young Generation");
        stat = new GCStat(bean);
    }

    @Test
    void garbageCollectorName() {
        assertThat(stat.garbageCollectorName("G1 Young Generation"))
                .isEqualTo("g1_young_generation");
        assertThat(stat.garbageCollectorName("G1 Old Generation"))
                .isEqualTo("g1_old_generation");
    }

    @Test
    void elapsed() {
        when(bean.getCollectionTime()).thenReturn(1000L, 3000L);
        assertThat(stat.elapsed()).isEqualTo(Duration.ofMillis(1000).toNanos());
        assertThat(stat.elapsed()).isEqualTo(Duration.ofMillis(2000).toNanos());
    }

    @Test
    void count() {
        when(bean.getCollectionCount()).thenReturn(1L, 3L);
        assertThat(stat.count()).isEqualTo(1);
        assertThat(stat.count()).isEqualTo(2);
    }
}

package core.framework.internal.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class GCStatTest {
    private GCStat stat;

    @BeforeEach
    void createGCStat() {
        stat = new GCStat(ManagementFactory.getGarbageCollectorMXBeans().get(0));
    }

    @Test
    void garbageCollectorName() {
        assertThat(stat.garbageCollectorName("G1 Young Generation"))
                .isEqualTo("g1_young_generation");
        assertThat(stat.garbageCollectorName("G1 Old Generation"))
                .isEqualTo("g1_old_generation");
    }
}

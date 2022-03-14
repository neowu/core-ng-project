package core.framework.internal.module;

import core.framework.util.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class ReadinessProbeTest {
    private ReadinessProbe probe;

    @BeforeEach
    void createReadinessProbe() {
        probe = new ReadinessProbe();
    }

    @Test
    void hostname() {
        assertThat(probe.hostname("redis")).isEqualTo("redis");
        assertThat(probe.hostname("kafka:9092")).isEqualTo("kafka");
    }

    @Test
    void check() throws Exception {
        probe.hostURIs.add("localhost");
        probe.check();
    }

    @Test
    void resolveHost() {
        StopWatch watch = mock(StopWatch.class);
        when(watch.elapsed()).thenReturn(ReadinessProbe.MAX_WAIT_TIME_IN_NANO);
        assertThatThrownBy(() -> probe.resolveHost("notExistedHost", watch))
            .isInstanceOf(Error.class)
            .hasMessageContaining("readiness check failed");
    }
}

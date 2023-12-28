package core.framework.internal.web.http;

import core.framework.internal.web.http.RateControl.Rate;
import core.framework.web.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RateControlTest {
    @Test
    void acquire() {
        var rate = new Rate(1);
        rate.lastUpdateTime = 100;
        boolean result = rate.acquire(101, 2, 1);
        assertThat(result).isTrue();
        assertThat(rate.currentPermits).isEqualTo(1);

        result = rate.acquire(100, 2, 1);
        assertThat(result).isTrue();
        assertThat(rate.currentPermits).isEqualTo(0);

        result = rate.acquire(101, 2, 1);
        assertThat(result).isFalse();

        result = rate.acquire(102, 2, 1);
        assertThat(result).isTrue();
        assertThat(rate.currentPermits).isEqualTo(0);
    }

    @Test
    void ratePerNano() {
        var control = new RateControl();
        control.maxEntries(1);
        assertThat(control.ratePerNano(100_000, Duration.ofMillis(1))).isEqualTo(0.1);
        assertThat(control.ratePerNano(1, Duration.ofSeconds(1))).isEqualTo(0.000000001);
        assertThat(control.ratePerNano(100, Duration.ofNanos(1))).isEqualTo(100);
        assertThat(control.ratePerNano(100, Duration.ofMinutes(5))).isEqualTo(100 / (5 * 60 * 1_000_000_000D));
    }

    @Test
    void validateRate() {
        var control = new RateControl();
        control.maxEntries(1);
        control.config("group", 1, 1, Duration.ofHours(24));
        control.validateRate("group", "10.0.0.1");

        assertThatThrownBy(() -> control.validateRate("group", "10.0.0.1"))
            .isInstanceOf(TooManyRequestsException.class)
            .hasMessageContaining("exceeded");
    }
}

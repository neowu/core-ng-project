package core.framework.internal.web.http;

import core.framework.web.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class RateControlTest {
    @Test
    void acquire() {
        RateControl.Rate rate = new RateControl.Rate(1);
        rate.lastUpdateTime = 100;
        boolean result = rate.acquire(101, 2, 1);
        assertThat(result).isTrue();
        assertThat(rate.currentPermits).isEqualTo(1);

        result = rate.acquire(101, 2, 1);
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
        RateControl limiter = new RateControl(1);
        assertThat(limiter.ratePerNano(100, TimeUnit.MICROSECONDS)).isEqualTo(0.1);
        assertThat(limiter.ratePerNano(1, TimeUnit.SECONDS)).isEqualTo(0.000000001);
        assertThat(limiter.ratePerNano(100, TimeUnit.NANOSECONDS)).isEqualTo(100);
    }

    @Test
    void validateRate() {
        RateControl control = new RateControl(1);
        control.config("group", 1, 1, TimeUnit.DAYS);
        control.validateRate("group", "10.0.0.1");

        assertThatThrownBy(() -> control.validateRate("group", "10.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("exceeded");
    }
}

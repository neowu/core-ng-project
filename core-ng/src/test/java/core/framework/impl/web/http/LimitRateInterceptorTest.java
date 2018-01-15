package core.framework.impl.web.http;

import core.framework.web.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author neo
 */
class LimitRateInterceptorTest {
    private LimitRateInterceptor interceptor;

    @BeforeEach
    void createLimitRateInterceptor() {
        interceptor = new LimitRateInterceptor();
        interceptor.config("group", 1, 1, TimeUnit.DAYS);
    }

    @Test
    void validateRate() {
        interceptor.validateRate("group", "10.0.0.1");

        TooManyRequestsException exception = assertThrows(TooManyRequestsException.class, () -> interceptor.validateRate("group", "10.0.0.1"));
        assertThat(exception.getMessage()).contains("exceeded");
    }
}

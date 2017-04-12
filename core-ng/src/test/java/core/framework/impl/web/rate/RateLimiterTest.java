package core.framework.impl.web.rate;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author neo
 */
public class RateLimiterTest {
    @Test
    public void acquire() {
        RateLimiter.Rate rate = new RateLimiter.Rate(1);
        rate.lastUpdateTime = 100;
        boolean result = rate.acquire(101, 2, 1);
        assertTrue(result);
        assertEquals(1, rate.currentPermits, 0.000001);

        result = rate.acquire(101, 2, 1);
        assertTrue(result);
        assertEquals(0, rate.currentPermits, 0.000001);

        result = rate.acquire(101, 2, 1);
        assertFalse(result);

        result = rate.acquire(102, 2, 1);
        assertTrue(result);
        assertEquals(0, rate.currentPermits, 0.000001);
    }

    @Test
    public void ratePerNano() {
        RateLimiter limiter = new RateLimiter(1);
        assertEquals(0.1, limiter.ratePerNano(100, TimeUnit.MICROSECONDS), 0.0000000000001);
        assertEquals(0.000000001, limiter.ratePerNano(1, TimeUnit.SECONDS), 0.0000000000001);
        assertEquals(100, limiter.ratePerNano(100, TimeUnit.NANOSECONDS), 0.0000000000001);
    }
}
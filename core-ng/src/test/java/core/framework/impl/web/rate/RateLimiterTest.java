package core.framework.impl.web.rate;

import org.junit.Test;

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
}
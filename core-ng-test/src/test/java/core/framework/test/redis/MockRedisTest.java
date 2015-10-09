package core.framework.test.redis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class MockRedisTest {
    MockRedis redis;

    @Before
    public void createMockRedis() {
        redis = new MockRedis();
    }

    @Test
    public void get() {
        redis.set("key", "value");

        String value = redis.get("key");
        Assert.assertEquals("value", value);
    }
}
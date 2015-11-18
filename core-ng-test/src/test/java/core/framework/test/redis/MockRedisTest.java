package core.framework.test.redis;

import core.framework.api.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void mget() {
        redis.set("key1", "value1");
        redis.set("key2", "value2");

        List<String> values = redis.mget(Lists.newArrayList("key1", "key3", "key2"));
        Assert.assertEquals(Lists.newArrayList("value1", null, "value2"), values);
    }
}
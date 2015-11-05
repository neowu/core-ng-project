package core.framework.api.module;

import core.framework.api.util.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author neo
 */
public class CacheConfigTest {
    private CacheConfig config;

    @Before
    public void createCacheConfig() {
        config = new CacheConfig(null);
    }

    @Test
    public void cacheName() {
        Assert.assertEquals("list-string", config.cacheName(null, Types.list(String.class)));

        Assert.assertEquals("string", config.cacheName(null, String.class));

        Assert.assertEquals("name", config.cacheName("name", String.class));
    }
}
package core.framework.module;

import core.framework.util.Types;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class CacheConfigTest {
    @Test
    public void cacheName() {
        Assert.assertEquals("list-string", CacheConfig.cacheName(null, Types.list(String.class)));

        Assert.assertEquals("string", CacheConfig.cacheName(null, String.class));

        Assert.assertEquals("name", CacheConfig.cacheName("name", String.class));
    }
}

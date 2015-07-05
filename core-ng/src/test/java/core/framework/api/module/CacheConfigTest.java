package core.framework.api.module;

import core.framework.api.util.Types;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class CacheConfigTest {
    CacheConfig cacheConfig = new CacheConfig(null);

    @Test
    public void cacheName() {
        String name = cacheConfig.cacheName(null, Types.list(String.class));

        Assert.assertEquals("list-string", name);
    }
}
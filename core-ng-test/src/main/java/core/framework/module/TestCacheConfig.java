package core.framework.module;

import org.jspecify.annotations.Nullable;

/**
 * @author neo
 */
public class TestCacheConfig extends CacheConfig {
    @Override
    void configureRedis(String host, @Nullable String password) {
        local();
    }
}

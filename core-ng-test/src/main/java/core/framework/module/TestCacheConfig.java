package core.framework.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public class TestCacheConfig extends CacheConfig {
    TestCacheConfig(ModuleContext context) {
        super(context);
    }

    @Override
    void configureRedis(String host) {
        local();
    }
}

package core.framework.module;

/**
 * @author neo
 */
public class TestCacheConfig extends CacheConfig {
    @Override
    void configureRedis(String host, String password) {
        local();
    }
}

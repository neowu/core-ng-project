package core.framework.module;

import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class CacheConfigTest {
    private CacheConfig config;

    @BeforeEach
    void createCacheConfig() {
        config = new CacheConfig();
        config.initialize(new ModuleContext(new LogManager()), null);
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("cache is configured but no cache added");
    }
}

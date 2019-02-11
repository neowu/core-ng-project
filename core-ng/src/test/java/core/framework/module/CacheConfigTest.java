package core.framework.module;

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
    }

    @Test
    void validate() {
        assertThatThrownBy(() -> config.validate())
                .hasMessageContaining("cache is configured but no cache added");
    }
}

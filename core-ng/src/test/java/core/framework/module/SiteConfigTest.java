package core.framework.module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class SiteConfigTest {
    private SiteConfig config;

    @BeforeEach
    void createSiteConfig() {
        config = new SiteConfig();
    }

    @Test
    void enableWebSecurity() {
        config.webSecurityConfigured = true;
        assertThatThrownBy(() -> config.webSecurity("default-src *;"))
                .hasMessageContaining("already configured");
    }
}

package core.framework.module;

import core.framework.util.Lists;
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
    void message() {
        config.messageConfigured = true;
        assertThatThrownBy(() -> config.message(Lists.newArrayList()))
                .hasMessageContaining("be configured once");
    }
}

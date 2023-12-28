package core.framework.module;

import core.framework.internal.module.ModuleContext;
import core.framework.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author neo
 */
class SiteConfigTest {
    private SiteConfig config;
    private ModuleContext context;

    @BeforeEach
    void createSiteConfig() {
        config = new SiteConfig();
        context = new ModuleContext(null);
        config.initialize(context, null);
    }

    @Test
    void message() {
        config.messageConfigured = true;
        assertThatThrownBy(() -> config.message(Lists.newArrayList()))
            .hasMessageContaining("be configured once");
    }

    @Test
    void cdn() {
        config.cdn().host("cdn");

        assertThat(context.httpServer.siteManager.templateManager.cdnManager.url("/app.css")).isEqualTo("//cdn/app.css");
    }
}

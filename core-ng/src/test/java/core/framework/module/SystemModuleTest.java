package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SystemModuleTest {
    private SystemModule module;

    @BeforeEach
    void createSystemModule() {
        module = new SystemModule(null);
        module.context = new ModuleContext(null);

        System.clearProperty("sys.http.listen");
    }

    @Test
    void configureHTTP() {
        module.configureHTTP();

        assertThat(module.context.httpServerConfig.httpHost).isNull();
        assertThat(module.context.httpServerConfig.httpsHost).isNull();
    }

    @Test
    void configureHTTPPortFromSystemProperty() {
        System.setProperty("sys.http.listen", "8081");
        module.context.propertyManager.properties.set("sys.http.listen", "8082");

        module.configureHTTP();
        assertThat(module.context.httpServerConfig.httpHost.port()).isEqualTo(8081);
    }

    @Test
    void configureHTTPPortFromProperty() {
        module.context.propertyManager.properties.set("sys.https.listen", "8082");

        module.configureHTTP();
        assertThat(module.context.httpServerConfig.httpHost).isNull();
        assertThat(module.context.httpServerConfig.httpsHost.port()).isEqualTo(8082);
    }

    @Test
    void configureSite() {
        module.context.propertyManager.properties.set("sys.security.csp", "default-src 'self';");
        module.configureSite();
        assertThat(module.site().security().interceptor.contentSecurityPolicy).isEqualTo("default-src 'self';");
    }
}

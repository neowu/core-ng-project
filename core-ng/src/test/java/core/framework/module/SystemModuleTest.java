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

        System.clearProperty("sys.http.port");
    }

    @Test
    void configureHTTP() {
        module.configureHTTP();

        assertThat(module.context.httpServer.httpPort).isNull();
        assertThat(module.context.httpServer.httpsPort).isNull();
    }

    @Test
    void configureHTTPPortFromSystemProperty() {
        System.setProperty("sys.http.port", "8081");
        module.context.propertyManager.properties.set("sys.http.port", "8082");

        module.configureHTTP();
        assertThat(module.context.httpServer.httpPort).isEqualTo(8081);
    }

    @Test
    void configureHTTPPortFromProperty() {
        module.context.propertyManager.properties.set("sys.https.port", "8082");

        module.configureHTTP();
        assertThat(module.context.httpServer.httpPort).isNull();
        assertThat(module.context.httpServer.httpsPort).isEqualTo(8082);
    }

    @Test
    void configureSite() {
        module.context.propertyManager.properties.set("sys.security.csp", "default-src 'self';");
        module.configureSite();
        assertThat(module.site().security().interceptor.contentSecurityPolicy).isEqualTo("default-src 'self';");
    }
}

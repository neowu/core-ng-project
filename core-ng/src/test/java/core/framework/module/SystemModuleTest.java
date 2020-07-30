package core.framework.module;

import core.framework.internal.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class SystemModuleTest {
    private SystemModule systemModule;

    @BeforeEach
    void createSystemModule() {
        systemModule = new SystemModule(null);
        systemModule.context = new ModuleContext(null);

        System.clearProperty("sys.http.port");
    }

    @Test
    void configureHTTP() {
        systemModule.configureHTTP();

        assertThat(systemModule.context.httpServer.httpPort).isNull();
        assertThat(systemModule.context.httpServer.httpsPort).isNull();
    }

    @Test
    void configureHTTPPortFromSystemProperty() {
        System.setProperty("sys.http.port", "8081");
        systemModule.context.propertyManager.properties.set("sys.http.port", "8082");

        systemModule.configureHTTP();
        assertThat(systemModule.context.httpServer.httpPort).isEqualTo(8081);
    }

    @Test
    void configureHTTPPortFromProperty() {
        systemModule.context.propertyManager.properties.set("sys.https.port", "8082");

        systemModule.configureHTTP();
        assertThat(systemModule.context.httpServer.httpPort).isNull();
        assertThat(systemModule.context.httpServer.httpsPort).isEqualTo(8082);
    }

    @Test
    void configureSite() {
        systemModule.context.propertyManager.properties.set("sys.security.csp", "default-src 'self';");
        systemModule.configureSite();
        assertThat(systemModule.site().security().interceptor.contentSecurityPolicy).isEqualTo("default-src 'self';");
    }
}

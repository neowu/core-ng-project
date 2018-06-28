package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author neo
 */
class SystemModuleTest {
    private SystemModule systemModule;

    @BeforeEach
    void createSystemModule() {
        systemModule = new SystemModule(null);
        systemModule.context = new ModuleContext();

        System.clearProperty("sys.http.port");
    }

    @Test
    void configureHTTP() {
        systemModule.configureHTTP();

        assertNull(systemModule.context.httpServer.httpPort);
        assertNull(systemModule.context.httpServer.httpsPort);
    }

    @Test
    void configureHTTPPortFromSystemProperty() {
        System.setProperty("sys.http.port", "8081");
        systemModule.context.propertyManager.properties.set("sys.http.port", "8082");

        systemModule.configureHTTP();
        assertEquals((Integer) 8081, systemModule.context.httpServer.httpPort);
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

    @Test
    void loadProperties() {
        var properties = new Properties();
        properties.set("sys.notAllowedKey", "value");

        assertThatThrownBy(() -> systemModule.loadProperties(properties))
                .isInstanceOf(Error.class)
                .hasMessageContaining("found unknown")
                .hasMessageContaining("allowedKeys=");
    }
}

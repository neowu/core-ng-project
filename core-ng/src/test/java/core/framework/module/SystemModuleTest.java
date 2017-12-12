package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        systemModule.context = new ModuleContext(new BeanFactory(), new TestMockFactory());

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
        assertNull(systemModule.context.httpServer.httpPort);
        assertEquals((Integer) 8082, systemModule.context.httpServer.httpsPort);
    }
}

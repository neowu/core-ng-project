package core.framework.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author neo
 */
class SystemModuleTest {
    private SystemModule systemModule;
    private ModuleContext context;

    @BeforeEach
    void createSystemModule() {
        context = spy(new ModuleContext(new BeanFactory()));

        systemModule = new SystemModule(null);
        systemModule.context = context;

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

    @Test
    void configureSite() {
        SiteConfig config = mock(SiteConfig.class);
        when(systemModule.context.config(SiteConfig.class, null)).thenReturn(config);

        systemModule.context.propertyManager.properties.set("sys.webSecurity.trustedSources", "https://cdn1,https://cdn2");
        systemModule.configureSite();

        verify(config).webSecurity("https://cdn1", "https://cdn2");
    }
}

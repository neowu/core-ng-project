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
    }

    @Test
    void httpPort() {
        assertNull(systemModule.httpPort("sys.http.httpPort"));
    }

    @Test
    void httpPortFromSystemProperty() {
        System.setProperty("sys.http.httpPort", "8081");
        assertEquals((Integer) 8081, systemModule.httpPort("sys.http.httpPort"));
    }

    @Test
    void httpPortFromProperty() {
        systemModule.context.properties.set("sys.http.httpPort", "8082");
        assertEquals((Integer) 8082, systemModule.httpPort("sys.http.httpPort"));
    }
}
